package org.apized.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apized.auth.api.role.Role;
import org.apized.auth.api.role.RoleRepository;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.core.security.MemoryUserResolver;
import org.apized.core.security.UserResolver;
import org.apized.micronaut.server.ApizedStartupEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Singleton
@Replaces(MemoryUserResolver.class)
public class DBUserResolver implements UserResolver {

  final static private String issuer = "apized";
  private final UserRepository userRepository;
  private ApplicationContext applicationContext;
  private RoleRepository roleRepository;
  private final int tokenDuration;
  private String domain;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  @Getter
  private Role defaultRole;

  DBUserResolver(
    ApplicationContext applicationContext,
    UserRepository userRepository,
    RoleRepository roleRepository,
    @Value("${auth.token.secret}") String secret,
    @Value("${auth.token.duration}") int duration,
    @Value("${auth.cookie.domain}") String domain
  ) {
    this.applicationContext = applicationContext;
    this.roleRepository = roleRepository;
    this.tokenDuration = duration;
    this.userRepository = userRepository;

    this.algorithm = Algorithm.HMAC256(secret);
    this.domain = domain;
    verifier = JWT.require(algorithm)
      .withIssuer(issuer)
      .withAudience(issuer)
      .withClaimPresence("iat")
      .withClaimPresence("sub")
      .build();
  }

  @Override
  public org.apized.core.security.model.User getUser(String token) {
    Optional<User> user;

    try {
      user = userRepository.get(
        token != null && !token.isBlank()
          ? UUID.fromString(verifier.verify(token).getSubject())
          : UUID.randomUUID()
      );
    } catch (Exception e) {
      user = Optional.empty();
    }

    return user
      .map(AuthConverter::convertAuthUserToApizedUser)
      .orElseGet(() -> new org.apized.core.security.model.User(
        UUID.randomUUID(),
        String.format("anonymous@%s", domain),
        "Anonymous",
        List.of(AuthConverter.convertAuthRoleToApizedRole(defaultRole)),
        List.of(),
        List.of(),
        Map.of()
      ));
  }

  @Override
  public org.apized.core.security.model.User getUser(UUID userId) {
    Optional<User> user = userRepository.get(userId);
    return user.map(AuthConverter::convertAuthUserToApizedUser).orElse(null);
  }

  @Override
  public String generateToken(org.apized.core.security.model.User user, boolean expiring) {
    Date issuedAt = new Date();
    JWTCreator.Builder builder = JWT.create()
      .withIssuer(issuer)
      .withAudience(issuer)
      .withJWTId(UUID.randomUUID().toString())
      .withIssuedAt(issuedAt)
      .withSubject(user.getId().toString());

    if (expiring) {
      LocalDateTime expiry = issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusSeconds(tokenDuration);
      builder.withExpiresAt(Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant()));
    }

    return builder.sign(algorithm);
  }

  @EventListener
  public void onStartup(ApizedStartupEvent event) {
    ensureDefaultRole();
    ensureAdministrator();
    applicationContext.getEventPublisher(AuthStartupEvent.class).publishEvent(new AuthStartupEvent());
  }

  private void ensureDefaultRole() {
    defaultRole = roleRepository.findDefaultRole().or(() ->
      {
        Role role = new Role();
        role.setName("Default");
        role.setDescription("The default role contains the permissions any user should get, including anonymous access");
        role.setPermissions(List.of(
          "auth.user.create",
          "auth.oauth.list",
          "auth.oauth.get"
        ));
        role.getMetadata().put("default", true);
        return Optional.ofNullable(roleRepository.create(role));
      }
    ).get();
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
      }
    );
  }
}
