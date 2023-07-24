package org.apized.auth.integration.mocks

import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apized.auth.api.user.User
import org.apized.core.behaviour.AbstractApiBehaviourHandler
import org.apized.core.behaviour.BehaviourManager
import org.apized.core.execution.Execution
import org.apized.core.model.Action
import org.apized.core.model.Layer
import org.apized.core.model.When

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
