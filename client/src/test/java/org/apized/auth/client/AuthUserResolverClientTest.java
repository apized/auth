package org.apized.auth.client;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.client.annotation.Client;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthUserResolverClientTest {
  @Test
  void usesStableClientIdConfiguredWithFederationAuthBaseUrl() {
    Client client = AuthUserResolverClient.class.getAnnotation(Client.class);

    assertEquals("auth", client.id());
    assertDoesNotThrow(() -> {
      try (ApplicationContext context = ApplicationContext.run(Map.of(
        "apized.federation.auth.base-url", "http://localhost:8080",
        "micronaut.http.services.auth.url", "${apized.federation.auth.base-url}"
      ))) {
        context.getBean(AuthUserResolverClient.class);
      }
    });
  }
}
