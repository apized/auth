package org.apized.auth.api.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import lombok.*;
import org.apized.auth.api.user.User;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
@Apized(extensions = {RoleRepositoryExtension.class})
public class Role extends BaseModel {
  /**
   * The name of this role.
   */
  @NotBlank
  protected String name;

  /**
   * Describes what the role represents.
   */
  protected String description;

  /**
   * The permissions that are to be granted to users that have this role. Read more about permissions at https://apized.org/#/documentation?id=security.
   */
  @TypeDef(type = DataType.JSON)
  List<String> permissions = new ArrayList<>();

  @JsonIgnore
  @ManyToMany(mappedBy = "roles")
  @JoinTable(
    name = "users_roles",
    joinColumns = @JoinColumn(name = "roles_id"),
    inverseJoinColumns = @JoinColumn(name = "users_id")
  )
  List<User> users = new ArrayList<>();
}
