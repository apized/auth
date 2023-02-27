package org.apized.auth.api.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;
import org.apized.core.model.Model;

import java.util.UUID;

@Getter
@Setter
@Serdeable
public class PasswordLoginRequest implements Model {
  @JsonIgnore
  private UUID id;

  /**
   * The username of the user to log in with.
   */
  String username;

  /**
   * The password for the provided username
   */
  String password;
}
