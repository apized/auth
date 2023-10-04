package org.apized.auth.api.oauth;

import org.apized.core.model.Apized;
import org.apized.core.model.Layer;

import java.util.Optional;

@Apized.Extension(layer = Layer.REPOSITORY)
public interface OauthRepositoryExtension {
  Optional<Oauth> findBySlug(String slug);
}
