package org.apized.auth.oauth;

import io.micronaut.core.type.Argument;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apized.auth.api.oauth.Oauth;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserService;
import org.apized.auth.security.AuthConverter;
import org.apized.auth.security.DBUserResolver;
import org.apized.core.context.ApizedContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class AuthOauthClient implements OauthClient {
  @Inject
  UserService userService;

  @Inject
  DBUserResolver userResolver;

  @Inject
  ObjectMapper mapper;

  HttpClient client = HttpClient.newHttpClient();

  @SneakyThrows
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public User getUser(Oauth oauth, String code, Map<String, Object> defaults, String redirect) {
    try {
      Argument<Map> responseType = Argument.of(Map.class, String.class, Object.class);
      String accessUrl = oauth.getAccessTokenUrl() + "&code=" + code + "&redirect_uri=" + redirect;

      Map<String, Object> props = new HashMap<>(
        Arrays.stream((accessUrl).split("\\?")[1].split("&"))
          .map(it -> it.split("="))
          .collect(Collectors.toMap(it -> it[0], it -> it[1]))
      );
      props.put("client_id", oauth.getClientId());
      props.put("client_secret", oauth.getComputedClientSecret());

      if (oauth.getProvider().equals(OauthProvider.Microsoft)) {
        defaults.putAll(
          mapper.readValue(client.send(
            HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(props.entrySet().stream().map((e) -> String.format("%s=%s", e.getKey(), e.getValue())).collect(Collectors.joining("&"))))
              .uri(new URI(applyPropsToString(accessUrl, props)))
              .build(),
            HttpResponse.BodyHandlers.ofString()
          ).body(), responseType)
        );
      } else {
        defaults.putAll(
          mapper.readValue(client.send(
            HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(props)))
              .uri(new URI(applyPropsToString(accessUrl, props)))
              .build(),
            HttpResponse.BodyHandlers.ofString()
          ).body(), responseType)
        );
      }
      props.put("token", oauth.getAccessTokenFrom(defaults));

      Map<String, Object> userResponse = oauth.getUserUrl() != null && !oauth.getUserUrl().isBlank() ?
        mapper.readValue(client.send(
            HttpRequest.newBuilder()
              .GET()
              .uri(new URI(applyPropsToString(oauth.getUserUrl(), props)))
              .headers(headersFor(oauth.getUserHeaders(), props))
              .build(),
            HttpResponse.BodyHandlers.ofString()
          )
          .body(), responseType) : defaults;

      Map<String, Object> emailResponse = oauth.getEmailUrl() != null && !oauth.getEmailUrl().isBlank() ?
        mapper.readValue(client.send(
            HttpRequest.newBuilder()
              .GET()
              .uri(new URI(applyPropsToString(oauth.getEmailUrl(), defaults)))
              .headers(headersFor(oauth.getEmailHeaders(), defaults))
              .build(),
            HttpResponse.BodyHandlers.ofString()
          )
          .body(), responseType) : userResponse;

      List<String> namePath = oauth.getMapping() != null && oauth.getMapping().get("name") != null ? List.of(oauth.getMapping().get("name").split(" ")) : List.of("name");
      List<String> emailPath = oauth.getMapping() != null && oauth.getMapping().get("email") != null ? List.of(oauth.getMapping().get("email").split(" ")) : List.of("email");

      String name = namePath.stream().map(path -> fetchPathInObject(path, userResponse)).collect(Collectors.joining(" "));
      String email = emailPath.stream().map(path -> fetchPathInObject(path, emailResponse)).collect(Collectors.joining(" ")).toLowerCase();

      return userService.findByUsername(email).orElseGet(() -> {
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername(email);
        newUser.setPassword(UUID.randomUUID().toString());
        newUser.setName(name);
        newUser.setVerified(true);
        newUser.setRoles(List.of(userResolver.getDefaultRole()));
        org.apized.core.security.model.User user = AuthConverter.convertAuthUserToApizedUser(newUser);
        user.getInferredPermissions().add("*");

        ApizedContext.getSecurity().setUser(user);
        userService.create(newUser);

        return newUser;
      });
    } catch (HttpClientResponseException e) {
      log.error(e.getResponse().getBody().toString());
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  private String fetchPathInObject(String path, Object obj) {
    for (String it : path.split("\\.")) {
      if (obj instanceof Map) {
        obj = ((Map<String, Object>) obj).get(it);
      } else {
        obj = ((List<Object>) obj).get(Integer.parseInt(it));
      }
    }
    return Optional.ofNullable(obj).map(Object::toString).orElse("");
  }

  private String[] headersFor(Map<String, String> headers, Map<String, Object> props) {
    List<String> result = new ArrayList<>();
    result.add("dummy");
    result.add("dummy");
    headers = headers != null ? headers : Collections.emptyMap();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      result.add(entry.getKey());
      result.add(applyPropsToString(entry.getValue(), props));
    }
    return result.toArray(new String[0]);
  }

  private String applyPropsToString(String string, Map<String, Object> props) {
    String result = string;
    for (String term : new Scanner(string).findAll("\\{(.*?)\\}").map(it -> it.group(1)).collect(Collectors.toSet())) {
      result = result.replaceAll("\\{" + term + "\\}", props.get(term).toString());
    }
    return result;
  }
}
