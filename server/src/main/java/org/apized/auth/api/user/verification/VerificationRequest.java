package org.apized.auth.api.user.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;
import org.apized.core.model.Model;

import java.util.UUID;

@Getter
@Setter
@Serdeable
public class VerificationRequest implements Model {
  @JsonIgnore
  private UUID id = UUID.randomUUID();

  private String code;
}
