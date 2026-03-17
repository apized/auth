package org.apized.auth.api.passkey;

import org.apized.core.model.Apized;
import org.apized.core.model.Layer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Apized.Extension(layer = Layer.REPOSITORY)
public interface PasskeyRepositoryExtension {
  List<Passkey> findByUserId(UUID userId);
  Optional<Passkey> findByCredentialId(String credentialId);
}
