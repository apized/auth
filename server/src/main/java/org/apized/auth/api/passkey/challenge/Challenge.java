package org.apized.auth.api.passkey.challenge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;
import org.apized.core.model.Layer;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Serdeable
@NoArgsConstructor
@Apized(
  layers = {Layer.SERVICE, Layer.REPOSITORY},
  extensions = {ChallengeRepositoryExtension.class},
  mcp = false
)
public class Challenge extends BaseModel {

  @Enumerated(EnumType.STRING)
  private ChallengeType type;

  /**
   * Set by the behaviour for REGISTRATION challenges.
   */
  private UUID userId;

  /**
   * Serialized WebAuthn options/request — not exposed in responses.
   */
  @JsonIgnore
  private String payload;

  /**
   * Returned in the CREATE response so the client can pass it to the WebAuthn API. Not persisted.
   */
  @Transient
  private Map<String, Object> options;

  /**
   * Optional username hint for AUTHENTICATION challenges. Write-only, not persisted.
   */
  @Transient
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String username;
}
