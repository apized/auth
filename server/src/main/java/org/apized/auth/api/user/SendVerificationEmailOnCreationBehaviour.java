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
//      emailSender.send(Email.builder()
//        .from("...")
//        .to(output.getUsername())
//        .subject("Please verify your email address")
//        .body(new TemplateBody<>(BodyType.HTML, new ModelAndView<>(
//          "verification",
//          Map.of(
//            "name", output.getName(),
//            "link", "https://google.com"
//          )
//        ))));
    }
  }
}
