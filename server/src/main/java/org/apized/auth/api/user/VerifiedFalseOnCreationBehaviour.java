package org.apized.auth.api.user;

import jakarta.inject.Singleton;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

@Behaviour(
  model = User.class,
  when = When.BEFORE,
  layer = Layer.SERVICE,
  actions = Action.CREATE
)
@Singleton
public class VerifiedFalseOnCreationBehaviour extends AbstractApiBehaviourHandler<User> {
  @Override
  public void preCreate(Execution execution, User input) {
    if (!ApizedContext.getSecurity().getUser().isAllowed("auth")) {
      input.setVerified(false);
    }
  }
}
