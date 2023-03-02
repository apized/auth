package org.apized.auth.api.role;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.user.UserRepository;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.UUID;

@Singleton
@Behaviour(
  model = Role.class,
  when = When.BEFORE,
  layer = Layer.SERVICE,
  actions = Action.DELETE
)
public class ClearUsersOnDeleteBehaviour extends AbstractApiBehaviourHandler<Role> {
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
