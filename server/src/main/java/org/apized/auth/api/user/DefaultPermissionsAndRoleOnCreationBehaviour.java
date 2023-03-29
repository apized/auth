package org.apized.auth.api.user;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.security.CodeGenerator;
import org.apized.auth.security.DBUserResolver;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import java.util.List;
import java.util.UUID;

@Behaviour(
  model = User.class,
  when = When.BEFORE,
  layer = Layer.SERVICE,
  actions = Action.CREATE
)
@Singleton
public class DefaultPermissionsAndRoleOnCreationBehaviour extends AbstractApiBehaviourHandler<User> {
  @Inject
  DBUserResolver resolver;

  @Override
  public void preCreate(Execution execution, User input) {
    if (input.getId() == null) {
      input.setId(UUID.randomUUID());
    }
    input.setPermissions(List.of(
      String.format("auth.user.get.%s", input.getId()),
      String.format("auth.user.update.%s.name", input.getId()),
      String.format("auth.user.update.%s.password", input.getId())
    ));
    input.setRoles(List.of(resolver.getDefaultRole()));

    if (!ApizedContext.getSecurity().getUser().isAllowed("auth")) {
      input.setVerified(false);
    }

    if (!input.isVerified()) {
      input.setEmailVerificationCode(CodeGenerator.generateCode());
    }
  }
}
