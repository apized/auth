package org.apized.auth.api.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apized.core.model.Model;

import java.util.UUID;

@Getter
@Setter
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Model {
  @JsonIgnore
  private UUID id;

  private String jwt;
}
