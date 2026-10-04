package org.apized.auth.api.apikey;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.user.UserRepository;
import org.apized.auth.security.DBUserResolver;
import org.apized.core.behaviour.BehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.ForbiddenException;
import org.apized.core.error.exception.UnauthorizedException;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.Page;
import org.apized.core.model.When;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Singleton
@Behaviour(
  model = ApiKey.class,
  layer = Layer.SERVICE,
  when = {When.BEFORE, When.AFTER},
  actions = {Action.CREATE, Action.GET, Action.LIST, Action.DELETE}
)
public class ApiKeyBehaviour implements BehaviourHandler<ApiKey> {
  private static final SecureRandom RANDOM = new SecureRandom();

  @Inject
  UserRepository userRepository;

  @Override
  public void preCreate(Execution<ApiKey> execution, ApiKey input) {
    rejectApiKeyManagement();
    input.setUserId(userRepository.get(ApizedContext.getSecurity().getUser().getId())
      .orElseThrow(() -> new UnauthorizedException("Authentication required to create API keys"))
      .getId());
    if (input.getId() == null) input.setId(UUID.randomUUID());
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    input.setSecretHash(DBUserResolver.hash(secret));
    input.setKey("ak_" + input.getId() + "_" + secret);
    input._getModelMetadata().getTouched().addAll(List.of("userId", "secretHash"));
  }

  @Override
  public void postCreate(Execution<ApiKey> execution, ApiKey input, ApiKey output) {
    output.setKey(input.getKey());
  }

  @Override
  public void postGet(Execution<ApiKey> execution, UUID id, ApiKey output) {
    output.setKey(null);
  }

  @Override
  public void postList(Execution<ApiKey> execution, Page<ApiKey> output) {
    output.getContent().forEach(key -> key.setKey(null));
  }

  @Override
  public void preDelete(Execution<ApiKey> execution, UUID id) {
    rejectApiKeyManagement();
  }

  private void rejectApiKeyManagement() {
    if (ApizedContext.getSecurity().getUser().getMetadata().containsKey("apiKeyId")) {
      throw new ForbiddenException("API keys cannot manage credentials", "auth.apiKey");
    }
  }
}
