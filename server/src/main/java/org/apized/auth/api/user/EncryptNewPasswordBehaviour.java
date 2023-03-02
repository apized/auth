package org.apized.auth.api.user;

import jakarta.inject.Singleton;
import org.apized.auth.security.BCrypt;
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
  actions = {Action.CREATE, Action.UPDATE},
  layer = Layer.SERVICE
)
@Singleton
public class EncryptNewPasswordBehaviour extends AbstractApiBehaviourHandler<User> {
  @Override
  public void preCreate(Execution execution, User input) {
    encryptPassword(input);
  }

  @Override
  public void preUpdate(Execution execution, UUID id, User input) {
    encryptPassword(input);
  }

  public void encryptPassword(User user) {
    if (user._getModelMetadata().getTouched().contains("password")) {
      String password = user.getPassword();
      if (password != null && !password.isBlank()) {
        if (password.length() < 8) {
          user.setPassword("invalid");
        } else {
          user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
      }
    }
  }
}
