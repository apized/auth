package org.apized.auth.api.user;

import jakarta.inject.Singleton;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

@Behaviour(
  model = User.class,
  when = When.AFTER,
  layer = Layer.SERVICE,
  actions = Action.CREATE
)
@Singleton
public class SendVerificationEmailOnCreationBehaviour extends AbstractApiBehaviourHandler<User> {
  @Override
  public void postCreate(Execution execution, User input, User output) {
    if (!output.isVerified()) {
      //todo send verification email
    }
  }
}
