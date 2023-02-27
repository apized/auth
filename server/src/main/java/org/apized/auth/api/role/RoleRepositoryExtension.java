package org.apized.auth.api.role;

import io.micronaut.data.annotation.Query;
import org.apized.core.model.Apized;
import org.apized.core.model.Layer;

import java.util.Optional;

@Apized.Extension(layer = Layer.REPOSITORY)
public interface RoleRepositoryExtension {

  @Query(value = "select * from role where (metadata ->> 'default')::bool = true", nativeQuery = true)
  Optional<Role> findDefaultRole();
}
