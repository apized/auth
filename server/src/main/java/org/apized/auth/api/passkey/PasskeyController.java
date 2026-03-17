package org.apized.auth.api.passkey;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.cookie.CookieFactory;
import io.micronaut.http.cookie.SameSite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apized.auth.api.passkey.challenge.Challenge;
import org.apized.auth.api.passkey.challenge.ChallengeService;
import org.apized.auth.api.passkey.challenge.ChallengeType;
import org.apized.auth.api.token.Token;
import org.apized.auth.api.user.User;
import org.apized.auth.security.AuthConverter;
import org.apized.auth.security.DBUserResolver;
import org.apized.core.ApizedConfig;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.UnauthorizedException;
import org.apized.core.model.Page;
import org.apized.core.search.SearchOperation;
import org.apized.core.search.SearchTerm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Controller("/passkeys")
public class PasskeyController {

  @Inject
  PasskeyService passkeyService;

  @Inject
  ChallengeService challengeService;

  @Inject
  PasskeyBehaviour passkeyBehaviour;

  @Inject
  DBUserResolver userResolver;

  @Inject
  ApizedConfig config;

  @Value("${auth.cookie.domain}")
  String cookieDomain;

  @Value("${auth.cookie.secure}")
  boolean secure;

  @Value("${auth.token.duration}")
  int tokenDuration;

  @Get
  @Operation(operationId = "ListPasskeys", tags = {"Passkey"}, summary = "List passkeys for the current user", security = @SecurityRequirement(name = "bearerAuth"))
  public Page<Passkey> list() {
    List<SearchTerm> search = List.of(
      SearchTerm.builder()
        .field("userId")
        .op(SearchOperation.eq)
        .value(ApizedContext.getSecurity().getUser().getId())
        .build()
    );
    return passkeyService.list(1, 50, search, List.of());
  }

  @Delete("/{id}")
  @Operation(operationId = "DeletePasskey", tags = {"Passkey"}, summary = "Delete a passkey", security = @SecurityRequirement(name = "bearerAuth"))
  public Passkey delete(UUID id) {
    return passkeyService.delete(id);
  }

  @Post("/challenges/register")
  @Operation(operationId = "PasskeyRegisterChallenge", tags = {"Passkey"}, summary = "Start passkey registration", description = "Returns a WebAuthn creation challenge. Pass the 'options' field to navigator.credentials.create().", security = @SecurityRequirement(name = "bearerAuth"))
  public Map<String, Object> registerChallenge() {
    Challenge challenge = new Challenge();
    challenge.setType(ChallengeType.REGISTRATION);
    challenge._getModelMetadata().getTouched().add("type");
    Challenge created = challengeService.create(challenge);
    return Map.of("challengeId", created.getId(), "options", created.getOptions());
  }

  @SuppressWarnings("unchecked")
  @Post("/register")
  @Operation(operationId = "PasskeyRegister", tags = {"Passkey"}, summary = "Finish passkey registration", description = "Submit the credential from navigator.credentials.create() to complete passkey registration.", security = @SecurityRequirement(name = "bearerAuth"))
  public Passkey register(@Body Map<String, Object> body) {
    Passkey passkey = new Passkey();
    passkey.setChallengeId(UUID.fromString((String) body.get("challengeId")));
    passkey.setCredential((Map<String, Object>) body.get("credential"));
    passkey._getModelMetadata().getTouched().addAll(List.of("challengeId", "credential"));
    return passkeyService.create(passkey);
  }

  @Post("/challenges/authentication")
  @Operation(operationId = "PasskeyAuthenticationChallenge", tags = {"Passkey"}, summary = "Start passkey authentication", description = "Returns a WebAuthn assertion challenge. Pass the 'options' field to navigator.credentials.get().")
  public Map<String, Object> authenticationChallenge(@Nullable @Body Map<String, String> body) {
    Challenge challenge = new Challenge();
    challenge.setType(ChallengeType.AUTHENTICATION);
    Optional.ofNullable(body).map(b -> b.get("username")).ifPresent(challenge::setUsername);
    challenge._getModelMetadata().getTouched().add("type");
    Challenge created = challengeService.create(challenge);
    return Map.of("challengeId", created.getId(), "options", created.getOptions());
  }

  @Post("/authentication")
  @Operation(operationId = "PasskeyAuthentication", tags = {"Passkey"}, summary = "Finish passkey authentication", description = "Submit the credential from navigator.credentials.get() to complete passkey authentication and receive a JWT.")
  public HttpResponse<Token> authentication(@Body Map<String, Object> body) {
    UUID challengeId = UUID.fromString((String) body.get("challengeId"));
    String credentialJson = passkeyBehaviour.toJson(body.get("credential"));
    User user = passkeyBehaviour.finishAuthentication(challengeId, credentialJson);
    if (!user.isVerified()) {
      throw new UnauthorizedException("Email verification pending");
    }
    return getHttpResponse(AuthConverter.convertAuthUserToApizedUser(user));
  }

  private MutableHttpResponse<Token> getHttpResponse(org.apized.core.security.model.User user) {
    Token token = new Token(null, userResolver.generateToken(user, true));
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
