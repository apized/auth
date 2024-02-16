package org.apized.auth.api.user;

import jakarta.inject.Singleton;
import org.apized.auth.security.CodeGenerator;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import java.util.UUID;

@Behaviour(
  model = User.class,
  when = When.BEFORE,
  layer = Layer.SERVICE,
  actions = {Action.CREATE, Action.UPDATE}
)
@Singleton
public class EmailValidationCodeBehaviour extends AbstractApiBehaviourHandler<User> {
  @Override
  public void preCreate(Execution execution, User input) {
    generateEmailValidationCode(input);
  }

  @Override
  public void preUpdate(Execution execution, UUID id, User input) {
    generateEmailValidationCode(input);
  }

  private void generateEmailValidationCode(User input) {
    if (!input.isVerified()) {
      input.setEmailVerificationCode(CodeGenerator.generateCode());
      input._getModelMetadata().getTouched().add("emailVerificationCode");
    } else if (input.getEmailVerificationCode() != null) {
      input.setEmailVerificationCode(null);
      input._getModelMetadata().getTouched().add("emailVerificationCode");
    }
  }
}
