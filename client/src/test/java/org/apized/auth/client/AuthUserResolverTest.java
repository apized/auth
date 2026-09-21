package org.apized.auth.client;

import org.apized.core.ApizedConfig;
import org.apized.core.security.model.User;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthUserResolverTest {
  @Test
  void propagatesAuthServiceFailuresWhenResolvingUserFromToken() {
    AuthUserResolver resolver = new AuthUserResolver();
    resolver.config = config();
    resolver.client = unavailableClient();

    assertThrows(IllegalStateException.class, () -> resolver.getUser("token"));
  }

  @Test
  void propagatesAuthServiceFailuresWhenResolvingUserById() {
    AuthUserResolver resolver = new AuthUserResolver();
    resolver.config = config();
    resolver.client = unavailableClient();

    assertThrows(IllegalStateException.class, () -> resolver.getUser(UUID.randomUUID()));
  }

  private ApizedConfig config() {
    ApizedConfig config = new ApizedConfig();
    config.setToken("service-token");
    return config;
  }

  private AuthUserResolverClient unavailableClient() {
    return new AuthUserResolverClient() {
      @Override
      public User getUser(String authorization, String token) {
        throw new IllegalStateException("auth service unavailable");
      }

      @Override
      public User getUser(String authorization, UUID userId) {
        throw new IllegalStateException("auth service unavailable");
      }

      @Override
      public Map<String, String> generateToken(String authorization, UUID userId, boolean expiring) {
        throw new IllegalStateException("auth service unavailable");
      }
    };
  }
}
