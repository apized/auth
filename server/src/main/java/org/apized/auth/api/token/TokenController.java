package org.apized.auth.api.token;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.cookie.CookieFactory;
import io.micronaut.http.cookie.SameSite;
import io.micronaut.serde.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apized.auth.api.oauth.Oauth;
import org.apized.auth.api.oauth.OauthService;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserService;
import org.apized.auth.oauth.OauthClient;
import org.apized.auth.security.AuthConverter;
import org.apized.auth.security.BCrypt;
import org.apized.auth.security.DBUserResolver;
import org.apized.core.ApizedConfig;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.error.exception.ForbiddenException;
import org.apized.core.error.exception.UnauthorizedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Introspected
@Transactional
@Controller("/tokens")
public class TokenController {
  @Inject
  ObjectMapper mapper;

  @Inject
  UserService userService;

  @Inject
  OauthService oauthService;

  @Inject
  OauthClient oauthClient;

  @Inject
  DBUserResolver userResolver;

  @Inject
  ApizedConfig config;

  @Value("${auth.backendUrl}")
  String backendUrl;

  @Value("${auth.frontendUrl}")
  String frontendUrl;

  @Value("${auth.cookie.domain}")
  String cookieDomain;

  @Value("${auth.cookie.secure}")
  boolean secure;

  @Value("${auth.token.duration}")
  int tokenDuration;

  @Post
  @Operation(
    operationId = "Login",
    tags = { "Token" },
    summary = "Login with username/password",
    description = """
         Login with a username/password pair
      """)
  public HttpResponse<Token> login(@Body PasswordLoginRequest login) {
    Optional<User> optionalUser = userService.findByUsername(login.getUsername());
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      if (BCrypt.checkpw(login.getPassword(), user.getPassword())) {
        if (user.isVerified()) {
          return getHttpResponse(AuthConverter.convertAuthUserToApizedUser(user));
        } else {
          throw new UnauthorizedException("Email verification pending");
        }
      } else {
        throw new UnauthorizedException("Not authorized");
      }
    } else {
      throw new UnauthorizedException("Not authorized");
    }
  }

  @Get("/oauth/{slug}")
  @Operation(
    operationId = "Oauth login",
    tags = { "Token" },
    summary = "Login with oauth",
    description = """
         Login with a oauth pair
      """)
  public HttpResponse<String> socialLogin(
    @PathVariable("slug") String slug,
    @QueryValue("code") String code
  ) {
    return oauthLogin(
      slug,
      code,
      new HashMap<>()
    );
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  @Post(value = "/oauth/{slug}", consumes = { "application/json", "application/x-www-form-urlencoded" })
  @Operation(
    operationId = "Oauth login",
    tags = { "Token" },
    summary = "Login with oauth",
    description = """
         Login with a oauth pair
      """)
  public HttpResponse<String> socialLogin(
    @PathVariable("slug") String slug,
    @Body Map<String, String> payload
  ) {
    return oauthLogin(
      slug,
      payload.get("code"),
      mapper.readValue(payload.getOrDefault("user", "{ \"name\": { \"firstName\": \"Unknown\", \"lastName\": \"User\" } }"), Map.class)
    );
  }

  @Delete
  @Operation(
    operationId = "Logout",
    tags = { "Token" },
    summary = "Logout",
    description = """
         Logout
      """)
  public HttpResponse<Token> logout() {
    return HttpResponse
      .ok()
      .body(new Token(null, ApizedContext.getSecurity().getToken()))
      .cookie(
        CookieFactory.INSTANCE.create(config.getCookie(), ApizedContext.getSecurity().getToken())
          .path("/")
          .maxAge(0)
          .domain(cookieDomain)
          .httpOnly(true)
          .sameSite(secure ? SameSite.None : SameSite.Lax)
          .secure(secure)
      );
  }

  @Post("/{userId}")
  @Operation(
    operationId = "Create",
    tags = { "Token" },
    summary = "Create token for user",
    security = @SecurityRequirement(name = "bearerAuth"),
    description = """
         Generate a token for the given user
      """)
  public Token create(UUID userId, @QueryValue(defaultValue = "true") boolean expiring) {
    if (
      (expiring && ApizedContext.getSecurity().getUser().getId().equals(userId)) ||
        ApizedContext.getSecurity().getUser().isAllowed("auth.token.create")
    ) {
      return new Token(
        null,
        userResolver.generateToken(AuthConverter.convertAuthUserToApizedUser(userService.get(userId)), expiring)
      );
    } else {
      throw new ForbiddenException("Not allowed to generate non-expiring tokens for other users", "auth.token.create");
    }
  }

  @Get("/")
  @Operation(
    operationId = "Redeem self token",
    tags = { "Token" },
    summary = "Redeem self token",
    security = @SecurityRequirement(name = "bearerAuth"),
    description = """
      """)
  public org.apized.core.security.model.User self() {
    return ApizedContext.getSecurity().getUser();
  }

  @Get("/{jwt}")
  @Operation(
    operationId = "Redeem a token",
    tags = { "Token" },
    summary = "Redeem a token",
    security = @SecurityRequirement(name = "bearerAuth"),
    description = """
      """)
  public org.apized.core.security.model.User redeem(String jwt) {
    org.apized.core.security.model.User user = userResolver.getUser(jwt);
    if (!ApizedContext.getSecurity().getUser().isAllowed("auth.token.redeem")) {
      throw new ForbiddenException("Not allowed to redeem tokens", "auth.token.redeem");
    }
    return user;
  }

  @Put("/{jwt}")
  @Operation(
    operationId = "Renew",
    tags = { "Token" },
    summary = "Renew a token",
    security = @SecurityRequirement(name = "bearerAuth"),
    description = """
      """)
  public HttpResponse<Token> renew(String jwt) {
    org.apized.core.security.model.User user = userResolver.getUser(jwt);
    if (
      !user.getId().equals(ApizedContext.getSecurity().getUser().getId()) &&
        !ApizedContext.getSecurity().getUser().isAllowed("auth.token.renew")
    ) {
      throw new ForbiddenException("Not allowed to renew tokens for other users", "auth.token.renew");
    }
    return getHttpResponse(user);
  }

  private MutableHttpResponse<String> oauthLogin(String slug, String code, Map<String, Object> props) {
    MutableHttpResponse<?> response;

    Oauth oauth = oauthService.findBySlug(slug).orElseThrow(() -> new BadRequestException("No such oauth " + slug));
    User user = oauthClient.getUser(oauth, code, props, String.format("%s/tokens/oauth/%s", backendUrl, slug));

    if (user != null) {
      if (!user.isVerified()) {
        user.setVerified(true);
        user.setEmailVerificationCode(null);
        userService.update(user.getId(), user);
      }
      response = getHttpResponse(AuthConverter.convertAuthUserToApizedUser(user));
    } else {
      response = HttpResponse.status(HttpStatus.BAD_REQUEST);
    }
    return response.contentType(MediaType.TEXT_HTML_TYPE)
      .body("<html><head></head><body><script>debugger;window.close();window.back();</script></body></html>");
  }

  private MutableHttpResponse<Token> getHttpResponse(org.apized.core.security.model.User user) {
    Token token = new Token(
      null,
      userResolver.generateToken(user, true)
    );
    return HttpResponse
      .ok()
      .body(token)
      .cookie(
        CookieFactory.INSTANCE.create(config.getCookie(), token.getJwt())
          .path("/")
          .maxAge(tokenDuration)
          .domain(cookieDomain)
          .httpOnly(true)
          .sameSite(secure ? SameSite.None : SameSite.Lax)
          .secure(secure)
      );
  }
}
