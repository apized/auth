package org.apized.auth.oauth;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.annotation.PostConstruct;
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
  ApplicationContext applicationContext;

  HttpClient client;

  @SneakyThrows
  @PostConstruct
  public void init() {
    client = applicationContext.createBean(HttpClient.class);
  }

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

      defaults.putAll(client.toBlocking().exchange(
        HttpRequest.POST(
          applyPropsToString(accessUrl, props),
          props
        ),
        responseType
      ).body());
      props.put("token", oauth.getAccessTokenFrom(defaults));

      Map<String, Object> userResponse = oauth.getUserUrl() != null && !oauth.getUserUrl().isBlank() ?
        client.toBlocking()
          .exchange(
            HttpRequest.GET(
                applyPropsToString(oauth.getUserUrl(), props)
              )
              .headers(
                applyPropsToHeaders(oauth.getUserHeaders(), props)
              ),
            responseType
          )
          .body() : defaults;

      Map<String, Object> emailResponse = oauth.getEmailUrl() != null && !oauth.getEmailUrl().isBlank() ?
        client.toBlocking()
          .exchange(
            HttpRequest.GET(
              applyPropsToString(oauth.getEmailUrl(), defaults)
            ).headers(
              applyPropsToHeaders(oauth.getEmailHeaders(), defaults)
            ),
            responseType
          )
          .body() : userResponse;

      List<String> namePath = oauth.getMapping() != null && oauth.getMapping().get("name") != null ? List.of(oauth.getMapping().get("name").split(" ")) : List.of("name");
      List<String> emailPath = oauth.getMapping() != null && oauth.getMapping().get("email") != null ? List.of(oauth.getMapping().get("email").split(" ")) : List.of("email");

      String name = namePath.stream().map(path -> fetchPathInObject(path, userResponse)).collect(Collectors.joining(" "));
      String email = emailPath.stream().map(path -> fetchPathInObject(path, emailResponse)).collect(Collectors.joining(" "));

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
    return obj.toString();
  }

  private Map<CharSequence, CharSequence> applyPropsToHeaders(Map<String, String> headers, Map<String, Object> props) {
    Map<CharSequence, CharSequence> result = new HashMap<>();
    headers = headers != null ? headers : Collections.emptyMap();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      result.put(entry.getKey(), applyPropsToString(entry.getValue(), props));
    }
    return result;
  }

  private String applyPropsToString(String string, Map<String, Object> props) {
    String result = string;
    for (String term : new Scanner(string).findAll("\\{(.*?)\\}").map(it -> it.group(1)).collect(Collectors.toSet())) {
      result = result.replaceAll("\\{" + term + "\\}", props.get(term).toString());
    }
    return result;
  }
}
