package org.apized.auth.api.user.password;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;
import org.apized.core.model.Model;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Setter
@Serdeable
public class PasswordResetRequest implements Model {
  @JsonIgnore
  private UUID id = UUID.randomUUID();

  @NotBlank
  private String password;

  @NotBlank
  private String code;
}
