package org.apized.auth.client;

import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apized.core.ApizedConfig;
import org.apized.core.security.MemoryUserResolver;
import org.apized.core.security.UserResolver;
import org.apized.core.security.model.User;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Singleton
@Replaces(MemoryUserResolver.class)
public class AuthUserResolver implements UserResolver {
  @Inject
  ApizedConfig config;

  @Inject
  AuthUserResolverClient client;

  @Override
  @SneakyThrows
  public User getUser(String token) {
    log.debug("Fetching user from token {}", token);
    return client.getUser(
      String.format("Bearer %s", config.getToken()),
      Optional.ofNullable(token).orElse(" ")
    );
  }

  @Override
  @SneakyThrows
  public User getUser(UUID userId) {
    log.debug("Fetching user by id {}", userId);
    return client.getUser(
      String.format("Bearer %s", config.getToken()),
      Optional.ofNullable(userId).orElse(UUID.randomUUID())
    );
  }

  @Override
  @SneakyThrows
  public String generateToken(User user, boolean expiring) {
    return client.generateToken(
      String.format("Bearer %s", config.getToken()),
      user.getId(),
      expiring
    ).get("token");
  }
}
