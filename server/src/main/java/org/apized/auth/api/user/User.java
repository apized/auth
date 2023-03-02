package org.apized.auth.api.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import lombok.*;
import org.apized.auth.api.role.Role;
import org.apized.core.model.Action;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
@Apized(
  operations = {Action.LIST, Action.GET, Action.CREATE, Action.UPDATE},
  extensions = {UserRepositoryExtension.class}
)
public class User extends BaseModel {

  /**
   * The username for this user. The username must be a valid email address.
   */
  @NotNull
  @Email
  @Size(min = 3)
  protected String username;

  /**
   * The name of this user.
   */
  @NotNull
  @Size(min = 2)
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
  @ManyToMany(cascade = CascadeType.ALL)
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
}
