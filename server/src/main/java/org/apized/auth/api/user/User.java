package org.apized.auth.api.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import lombok.*;
import org.apized.auth.BCrypt;
import org.apized.auth.api.role.Role;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Serdeable
@Apized(extensions = {UserRepositoryExtension.class})
public class User extends BaseModel {

  /**
   * The username for this user. The username must be a valid email address.
   */
  @NotNull
  @Email
  protected String username;

  /**
   * The name of this user.
   */
  @NotBlank
  protected String name;

  /**
   * Write-only property to set this user's password.
   */
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  protected String password;

  /**
   * Has the user's email address has been verified (via email with link & code).
   */
  protected boolean verified;

  /**
   * List of permissions attributed to this user. Read more about permissions at https://apized.org/#/documentation?id=security.
   */
  @TypeDef(type = DataType.JSON)
  List<String> permissions = new ArrayList<>();

  /**
   * List of roles associated with this user. Permissions of these roles will apply to this user.
   */
  @ManyToMany
  @JoinTable(
    name = "users_roles",
    joinColumns = @JoinColumn(name = "users_id"),
    inverseJoinColumns = @JoinColumn(name = "roles_id")
  )
  protected List<Role> roles = new ArrayList<>();


  @JsonIgnore
  String emailVerificationCode;

  @JsonIgnore
  String passwordResetCode;

  @SneakyThrows
  public void setPassword(String password) {
    if (password != null && !password.isBlank()) {
      this.password = BCrypt.hashpw(password, BCrypt.gensalt());
      if (password.length() < 8) {
        this.password = "invalid";
      }
    }
  }
}
