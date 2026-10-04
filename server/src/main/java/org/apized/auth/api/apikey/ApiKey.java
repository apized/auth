package org.apized.auth.api.apikey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apized.core.model.Action;
import org.apized.core.model.Apized;
import org.apized.core.model.BaseModel;
import org.apized.core.security.annotation.Owner;

import java.util.UUID;

@Getter
@Setter
@Entity
@Serdeable
@NoArgsConstructor
@Apized(
  operations = {Action.LIST, Action.GET, Action.CREATE, Action.DELETE},
  audit = false,
  event = false
)
public class ApiKey extends BaseModel {
  @JsonIgnore
  @Owner(actions = {Action.LIST, Action.GET, Action.CREATE, Action.DELETE})
  private UUID userId;

  @NotBlank
  private String name;

  @JsonIgnore
  private String secretHash;

  /** Only populated in the creation response; never persisted or accepted as input. */
  @Transient
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String key;
}
