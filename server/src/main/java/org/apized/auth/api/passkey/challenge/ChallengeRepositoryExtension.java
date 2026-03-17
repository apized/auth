package org.apized.auth.api.passkey.challenge;

import org.apized.core.model.Apized;
import org.apized.core.model.Layer;

import java.time.LocalDateTime;

@Apized.Extension(layer = Layer.REPOSITORY)
public interface ChallengeRepositoryExtension {
  void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
