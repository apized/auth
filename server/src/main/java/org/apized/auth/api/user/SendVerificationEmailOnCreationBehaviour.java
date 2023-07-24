package org.apized.auth.api.user;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.apized.auth.EmailService;
import org.apized.auth.SendgridEmailService;
import org.apized.core.behaviour.AbstractApiBehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import java.util.Map;

@Behaviour(
  model = User.class,
  when = When.AFTER,
  layer = Layer.SERVICE,
  actions = Action.CREATE
)
@Singleton
public class SendVerificationEmailOnCreationBehaviour extends AbstractApiBehaviourHandler<User> {

  @Value("${sendgrid.templates.user-verification}")
  private String templateId;

  @Inject
  private EmailService emailService;

  @Override
  @SneakyThrows
  public void postCreate(Execution execution, User input, User output) {
    if (!output.isVerified()) {
      emailService.send(
        templateId,
        output.getName(),
        output.getUsername(),
        Map.of(
          "name", output.getName(),
          "code", output.getEmailVerificationCode()
        )
      );
    }
  }
}
