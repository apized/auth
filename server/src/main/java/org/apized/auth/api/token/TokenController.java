package org.apized.auth.api.token;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.cookie.SameSite;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import org.apized.auth.BCrypt;
import org.apized.auth.DBUserResolver;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserService;
import org.apized.core.error.exception.UnauthorizedException;

import javax.transaction.Transactional;
import java.util.concurrent.atomic.AtomicReference;

@Introspected
@Transactional
@Controller("/token")
public class TokenController {
  @Inject
  UserService userService;

  @Inject
  DBUserResolver userResolver;

  @Value("${auth.domain}")
  String domain;

  @Value("${auth.duration}")
  int tokenDuration;

  @Post("/")
  @Operation(
    operationId = "Login",
    tags = {"Token"},
    summary = "Login with username/password",
    description = """
         Login with a username/password pair
      """)
  public org.apized.core.security.model.User create(@Body PasswordLoginRequest login) {
    AtomicReference<User> resolvedUser = new AtomicReference<>();
    userService.findByUsername(login.getUsername()).ifPresentOrElse(user -> {
      if (BCrypt.checkpw(login.getPassword(), user.getPassword())) {
        if (user.isVerified()) {
          resolvedUser.set(user);
          String token = userResolver.generateToken(userResolver.convertUser(resolvedUser.get()), true);
          HttpResponse.ok().cookie(
            new SimpleCookie("apized_auth", token)
              .path("/")
              .maxAge(tokenDuration)
              .domain(domain)
              .httpOnly(true)
              .sameSite(SameSite.None)
              .secure(true)
          );
        } else {
          // todo send verification email
          throw new UnauthorizedException("Email verification pending");
        }
      } else {
        throw new UnauthorizedException("Not authorized");
      }
    }, () -> {
      throw new UnauthorizedException("Not authorized");
    });
    return userResolver.convertUser(resolvedUser.get());
  }

  @Get("/{jwt}")
  @Operation(
    operationId = "Redeem",
    tags = {"Token"},
    summary = "Redeem a token",
    description = """
      """)
  public org.apized.core.security.model.User redeem(String jwt) {
    return userResolver.getUser(jwt);
  }

  @Put("/{jwt}")
  @Operation(
    operationId = "Renew",
    tags = {"Token"},
    summary = "Renew a token",
    description = """
      """)
  public org.apized.core.security.model.User renew(String jwt) {
    org.apized.core.security.model.User user = userResolver.getUser(jwt);
    String token = userResolver.generateToken(user, true);
    HttpResponse.ok().cookie(
      new SimpleCookie("apized_auth", token)
        .path("/")
        .maxAge(tokenDuration)
        .domain(domain)
        .httpOnly(true)
        .sameSite(SameSite.None)
        .secure(true)
    );
    return user;
  }
}
