package org.apized.auth.client;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;
import jakarta.validation.constraints.NotNull;
import org.apized.core.security.model.User;

import java.util.Map;
import java.util.UUID;

@Client("${apized.federation.auth}")
public interface AuthUserResolverClient {
  @Get("/tokens/{token}?fields=*,roles.id,roles.name,roles.permissions")
  User getUser(
    @Header(name = "Authorization") String authorization,
    @PathVariable("token") @NotNull String token
  );

  @Get("/users/{userId}?fields=*,roles.id,roles.name,roles.permissions")
  User getUser(
    @Header(name = "Authorization") String authorization,
    @PathVariable("userId") @NotNull UUID userId
  );

  @Get("/auth/users/{userId}/token?expiring={expiring}")
  Map<String, String> generateToken(
    @Header(name = "Authorization") String authorization,
    @PathVariable("userId") @NotNull UUID userId,
    @PathVariable("expiring") @NotNull boolean expiring
  );
}
