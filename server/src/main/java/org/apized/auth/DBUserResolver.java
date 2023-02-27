package org.apized.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.apized.auth.api.role.Role;
import org.apized.auth.api.role.RoleRepository;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.core.error.exception.UnauthorizedException;
import org.apized.core.security.MemoryUserResolver;
import org.apized.core.security.UserResolver;
import org.apized.micronaut.server.ApizedStartupEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
@Replaces(MemoryUserResolver.class)
public class DBUserResolver implements UserResolver {

  final static private String issuer = "apized";
  private final UserRepository userRepository;
  private RoleRepository roleRepository;
  private final int tokenDuration;
  private String domain;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;
  private Role defaultRole;

  DBUserResolver(
    UserRepository userRepository,
    RoleRepository roleRepository,
    @Value("${auth.secret}") String secret,
    @Value("${auth.duration}") int duration,
    @Value("${auth.domain}") String domain
  ) {
    this.roleRepository = roleRepository;
    this.tokenDuration = duration;
    this.userRepository = userRepository;

    this.algorithm = Algorithm.HMAC256(secret);
    this.domain = domain;
    verifier = JWT.require(algorithm)
      .withIssuer(issuer)
      .withAudience(issuer)
      .build();
  }

  @Override
  public org.apized.core.security.model.User getUser(String token) {
    Optional<User> user = userRepository.get(
      token != null
        ? UUID.fromString(verifier.verify(token).getSubject())
        : UUID.randomUUID()
    );

    if (user.isPresent()) {
      if (!user.get().isVerified()) {
        throw new UnauthorizedException("Not authorized");
      }

      return convertUser(user.get());
    } else {
      return new org.apized.core.security.model.User(
        null,
        "anonymous@apized.org",
        "Anonymous",
        List.of(convertRole(defaultRole)),
        List.of(),
        List.of()
      );
    }
  }

  @Override
  public org.apized.core.security.model.User getUser(UUID userId) {
    Optional<User> user = userRepository.get(userId);
    return user.map(this::convertUser).orElse(null);
  }

  @Override
  public org.apized.core.security.model.User ensureUser(org.apized.core.security.model.User user) {
    return null; // Not necessary for internal use
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

  public org.apized.core.security.model.User convertUser(User user) {
    return new org.apized.core.security.model.User(
      user.getId(),
      user.getUsername(),
      user.getName(),
      user.getRoles().stream().map(this::convertRole).toList(),
      user.getPermissions(),
      List.of()
    );
  }

  private org.apized.core.security.model.Role convertRole(Role role) {
    return new org.apized.core.security.model.Role(
      role.getId(),
      role.getName(),
      role.getDescription(),
      role.getPermissions()
    );
  }

  @EventListener
  void onStartup(ApizedStartupEvent event) {
    ensureDefaultRole();
    ensureAdministrator();
  }

  private void ensureDefaultRole() {
    defaultRole = roleRepository.findDefaultRole().or(() ->
      {
        Role role = new Role(
          "Default",
          "The default role contains the permissions any user should get, including anonymous access",
          List.of("auth.user.create"),
          List.of()
        );
        role.getMetadata().put("default", true);
        return Optional.ofNullable(roleRepository.create(role));
      }
    ).get();
  }

  private void ensureAdministrator() {
    String username = String.format("administrator@%s", domain);
    userRepository.findByUsername(username).or(() ->
      Optional.ofNullable(userRepository.create(new User(
        username,
        "Administrator",
        BCrypt.hashpw("changeme", BCrypt.gensalt()),
        true,
        List.of("*"),
        List.of(),
        null,
        null)
      )));
  }
}
