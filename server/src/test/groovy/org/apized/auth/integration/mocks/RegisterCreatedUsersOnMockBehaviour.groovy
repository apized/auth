package org.apized.auth.integration.mocks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.user.User;
import org.apized.auth.security.DBUserResolver;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.BehaviourManager;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Singleton
class RegisterCreatedUsersOnMockBehaviour extends AbstractApiBehaviourHandler<User> {

  @Inject
  BehaviourManager manager

  @Inject
  UserResolverMock resolver

  @PostConstruct
  void init() {
    manager.registerBehaviour(User, Layer.SERVICE, List.of(When.AFTER), List.of(Action.CREATE), 0, this)
  }

  @Override
  void postCreate(Execution execution, User input, User output) {
    resolver.registerUser(output)
  }
}
