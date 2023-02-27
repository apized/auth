package org.apized.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.apized.auth.api.role.Role;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.core.error.exception.UnauthorizedException;
import org.apized.core.security.MemoryUserResolver;
import org.apized.core.security.UserResolver;

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

  private Initializer initializer;
  private final int tokenDuration;
  private final UserRepository repository;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  DBUserResolver(
    Initializer initializer,
    UserRepository repository,
    @Value("${auth.secret}") String secret,
    @Value("${auth.duration}") int duration
  ) {
    this.initializer = initializer;
    this.tokenDuration = duration;
    this.repository = repository;

    this.algorithm = Algorithm.HMAC256(secret);
    verifier = JWT.require(algorithm)
      .withIssuer(issuer)
      .withAudience(issuer)
      .build();
  }

  @Override
  public org.apized.core.security.model.User getUser(String token) {
    Optional<User> user = repository.get(
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
        List.of(convertRole(initializer.getDefaultRole())),
        List.of(),
        List.of()
      );
    }
  }

  @Override
  public org.apized.core.security.model.User getUser(UUID userId) {
    Optional<User> user = repository.get(userId);
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
}
