package org.apized.auth.api.passkey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apized.auth.api.user.User;
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
  scope = {User.class},
  layers = {Layer.SERVICE, Layer.REPOSITORY},
  extensions = {PasskeyRepositoryExtension.class},
  mcp = false
)
public class Passkey extends BaseModel {
  @JsonIgnore
  @ManyToOne
  private User user;

  private String credentialId;

  @JsonIgnore
  private byte[] publicKeyCose;

  private long signCount;

  private String aaguid;

  private String transports;

  /**
   * Write-only: the ID of the PasskeyChallenge from POST /passkeys/challenges/register.
   */
  @Transient
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private UUID challengeId;

  /**
   * Write-only: the PublicKeyCredential JSON object from navigator.credentials.create().
   */
  @Transient
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Map<String, Object> credential;
}
