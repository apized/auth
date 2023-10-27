package org.apized.auth.api.oauth;

import jakarta.inject.Singleton;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.Page;
import org.apized.core.model.When;

import java.util.UUID;

@Behaviour(
  model = Oauth.class,
  when = When.AFTER,
  actions = {Action.GET, Action.LIST},
  layer = Layer.CONTROLLER
)
@Singleton
public class ProtectClientSecretBehaviour extends AbstractApiBehaviourHandler<Oauth> {
  @Override
  public void postList(Execution execution, Page<Oauth> output) {
    if (!ApizedContext.getSecurity().getUser().isAllowed("auth.oauth")) {
      output.getContent().forEach(this::protectClientSecret);
    }
  }

  @Override
  public void postGet(Execution execution, UUID id, Oauth output) {
    if (!ApizedContext.getSecurity().getUser().isAllowed("auth.oauth")) {
      protectClientSecret(output);
    }
  }

  private void protectClientSecret(Oauth oauth) {
    oauth.setClientSecret(null);
  }
}
