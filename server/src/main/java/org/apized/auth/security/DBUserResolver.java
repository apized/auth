package org.apized.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apized.auth.api.apikey.ApiKey;
import org.apized.auth.api.apikey.ApiKeyRepository;
import org.apized.auth.api.role.Role;
import org.apized.auth.api.role.RoleRepository;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.core.micronaut.ApizedStartupEvent;
import org.apized.core.security.MemoryUserResolver;
import org.apized.core.security.UserResolver;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.security.MessageDigest;

@Slf4j
@Singleton
@Replaces(MemoryUserResolver.class)
public class DBUserResolver implements UserResolver {
  private static final String issuer = "apized";
  private final UserRepository userRepository;
  private final ApiKeyRepository apiKeyRepository;
  private final ApplicationContext applicationContext;
  private final RoleRepository roleRepository;
  private final int tokenDuration;
  private final String domain;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  @Getter
  private Role defaultRole;

  DBUserResolver(
    ApplicationContext applicationContext,
    UserRepository userRepository,
    ApiKeyRepository apiKeyRepository,
    RoleRepository roleRepository,
    @Value("${auth.token.secret}") String secret,
    @Value("${auth.token.duration}") int duration,
    @Value("${auth.cookie.domain}") String domain
  ) {
    this.applicationContext = applicationContext;
    this.roleRepository = roleRepository;
    this.tokenDuration = duration;
    this.userRepository = userRepository;
    this.apiKeyRepository = apiKeyRepository;
    this.algorithm = Algorithm.HMAC256(secret);
    this.domain = domain;
    verifier = JWT.require(algorithm)
      .withIssuer(issuer)
      .withAudience(issuer)
      .withClaimPresence("iat")
      .withClaimPresence("sub")
      .withClaimPresence("exp")
      .build();
  }

  @Override
  public org.apized.core.security.model.User getUser(String token) {
    if (token != null && token.startsWith("ak_")) return resolveApiKey(token);
    Optional<User> user;
    try {
      user = userRepository.get(token != null && !token.isBlank()
        ? UUID.fromString(verifier.verify(token).getSubject())
        : UUID.randomUUID());
    } catch (JWTDecodeException e) {
      user = Optional.empty();
    }
    return user.map(AuthConverter::convertAuthUserToApizedUser).orElseGet(this::anonymousUser);
  }

  private org.apized.core.security.model.User resolveApiKey(String token) {
    String[] parts = token.split("_", 3);
    if (parts.length != 3) return anonymousUser();
    UUID id;
    try {
      id = UUID.fromString(parts[1]);
    } catch (IllegalArgumentException e) {
      return anonymousUser();
    }
    Optional<ApiKey> key = apiKeyRepository.get(id);
    if (key.isEmpty() ||
      !MessageDigest.isEqual(key.get().getSecretHash().getBytes(java.nio.charset.StandardCharsets.US_ASCII),
        hash(parts[2]).getBytes(java.nio.charset.StandardCharsets.US_ASCII))) return anonymousUser();

    return userRepository.get(key.get().getUserId()).map(user -> {
      org.apized.core.security.model.User principal = AuthConverter.convertAuthUserToApizedUser(user);
      principal.setMetadata(Map.of("apiKeyId", id.toString()));
      return principal;
    }).orElseGet(this::anonymousUser);
  }

  private org.apized.core.security.model.User anonymousUser() {
    return new org.apized.core.security.model.User(
      UUID.randomUUID(), String.format("anonymous@%s", domain), "Anonymous",
      List.of(AuthConverter.convertAuthRoleToApizedRole(defaultRole)), List.of(), List.of(), Map.of()
    );
  }

  public static String hash(String secret) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
        .digest(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public org.apized.core.security.model.User getUser(UUID userId) {
    return userRepository.get(userId).map(AuthConverter::convertAuthUserToApizedUser).orElse(null);
  }

  @Override
  public String generateToken(org.apized.core.security.model.User user, boolean ignored) {
    Date issuedAt = new Date();
    JWTCreator.Builder builder = JWT.create()
      .withIssuer(issuer)
      .withAudience(issuer)
      .withJWTId(UUID.randomUUID().toString())
      .withIssuedAt(issuedAt)
      .withSubject(user.getId().toString());
    LocalDateTime expiry = issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusSeconds(tokenDuration);
    builder.withExpiresAt(Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()));
    return builder.sign(algorithm);
  }

  @EventListener
  public void onStartup(ApizedStartupEvent event) {
    ensureDefaultRole();
    ensureAdministrator();
    applicationContext.getEventPublisher(AuthStartupEvent.class).publishEvent(new AuthStartupEvent());
  }

  private void ensureDefaultRole() {
    defaultRole = roleRepository.findDefaultRole().or(() -> {
      Role role = new Role();
      role.setName("Default");
      role.setDescription("The default role contains the permissions any user should get, including anonymous access");
      role.setPermissions(List.of(
        "auth.user.create",
        "auth.oauth.list",
        "auth.oauth.get",
        "auth.challenge.create",
        "auth.challenge.get",
        "auth.challenge.delete",
        "auth.passkey.create"
      ));
      role.getMetadata().put("default", true);
      return Optional.ofNullable(roleRepository.create(role));
    }).get();
  }

  private void ensureAdministrator() {
    String username = String.format("administrator@%s", domain);
    userRepository.findByUsername(username).or(() -> {
      User user = new User();
      user.setUsername(username);
      user.setName("Administrator");
      user.setPassword(BCrypt.hashpw("changeme", BCrypt.gensalt()));
      user.setVerified(true);
      user.setPermissions(List.of("*"));
      user = userRepository.create(user);
      log.info(String.format("Created admin user with id %s", user.getId()));
      return Optional.of(user);
    });
  }
}
