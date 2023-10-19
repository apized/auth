package org.apized.auth.client;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apized.core.ApizedConfig;
import org.apized.core.security.MemoryUserResolver;
import org.apized.core.security.UserResolver;
import org.apized.core.security.model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Singleton
@Replaces(MemoryUserResolver.class)
public class AuthUserResolver implements UserResolver {
  HttpClient client = HttpClient.newHttpClient();

  @Inject
  ObjectMapper mapper;

  @Inject
  ApizedConfig config;

  @Override
  @SneakyThrows
  public User getUser(String token) {
    log.debug("Fetching user from token {}", token);

    String uri = config.getFederation().get("auth") + "/tokens/" + token + "?fields=*,roles.id,roles.name,roles.permissions";
    HttpRequest request = HttpRequest.newBuilder()
      .uri(new URI(uri))
      .header("AUTHORIZATION", String.format("Bearer %s", config.getToken()))
      .build();

    return mapper.readValue(
      client.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      ).body(),
      User.class
    );
  }

  @Override
  @SneakyThrows
  public User getUser(UUID userId) {
    log.debug("Fetching user by id {}", userId);

    String uri = config.getFederation().get("auth") + "/users/" + userId + "?fields=*,roles.id,roles.name,roles.permissions";
    HttpRequest request = HttpRequest.newBuilder()
      .uri(new URI(uri))
      .header("AUTHORIZATION", String.format("Bearer %s", config.getToken()))
      .build();

    return mapper.readValue(
      client.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      ).body(),
      User.class
    );
  }

  @Override
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public String generateToken(User user, boolean expiring) {
    String uri = config.getFederation().get("auth") + "/auth/users/" + user.getId() + "/token?expiring=" + expiring;

    HttpRequest request = HttpRequest.newBuilder()
      .uri(new URI(uri))
      .header("AUTHORIZATION", String.format("Bearer %s", config.getToken()))
      .build();

    Map<String, String> map = mapper.readValue(
      client.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      ).body(),
      Argument.of(Map.class, String.class, String.class)
    );

    return map.get("token");
  }
}
