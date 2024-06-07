package org.apized.auth.api.role;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.apized.auth.api.user.UserRepository;
import org.apized.core.behaviour.BehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import java.util.UUID;

@Singleton
@Behaviour(
  model = Role.class,
  when = When.BEFORE,
  layer = Layer.SERVICE,
  actions = Action.DELETE
)
public class ClearUsersOnDeleteBehaviour implements BehaviourHandler<Role> {
  @Inject
  RoleRepository roleRepository;

  @Inject
  UserRepository userRepository;

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @Override
  public void preDelete(Execution execution, UUID id) {
    roleRepository.clearUsersFor(id);
  }
}
