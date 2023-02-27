package org.apized.auth.api.user;

import org.apized.core.model.Apized;
import org.apized.core.model.Layer;

import java.util.Optional;

@Apized.Extension(layer = Layer.REPOSITORY)
public interface UserRepositoryExtension {
  Optional<User> findByUsername(String username);
}
