package org.apized.auth.api.passkey.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.apized.auth.api.user.UserRepository;
import org.apized.auth.passkey.PasskeyConfig;
import org.apized.auth.passkey.PasskeyCredentialService;
import org.apized.core.behaviour.BehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@Behaviour(model = Challenge.class, layer = Layer.SERVICE, when = When.BEFORE, actions = Action.CREATE)
public class ChallengeBehaviour implements BehaviourHandler<Challenge> {
  private static final long TTL_SECONDS = 300;

  @Inject
  ObjectMapper objectMapper;

  @Inject
  PasskeyConfig config;

  @Inject
  UserRepository userRepository;

  @Inject
  ChallengeService challengeService;

  @Override
  @SneakyThrows
  public void preCreate(Execution execution, Challenge input) {
    challengeService.deleteByCreatedAtBefore(LocalDateTime.now().minusSeconds(TTL_SECONDS));

    if (ChallengeType.REGISTRATION == input.getType()) {
      var userId = ApizedContext.getSecurity().getUser().getId();
      var user = userRepository.get(userId).orElseThrow(() -> new BadRequestException("User not found"));

      PublicKeyCredentialCreationOptions options = config.getRelyingParty().startRegistration(
        StartRegistrationOptions.builder()
          .user(UserIdentity.builder()
            .name(user.getUsername())
            .displayName(user.getName())
            .id(PasskeyCredentialService.uuidToByteArray(userId))
            .build())
          .authenticatorSelection(
            AuthenticatorSelectionCriteria.builder()
              .residentKey(ResidentKeyRequirement.PREFERRED)
              .userVerification(UserVerificationRequirement.PREFERRED)
              .build()
          )
          .build()
      );

      input.setUserId(userId);
      input.setPayload(objectMapper.writeValueAsString(options));
      input.setOptions(objectMapper.readValue(objectMapper.writeValueAsString(options), Map.class));
      input._getModelMetadata().getTouched().addAll(List.of("userId", "payload"));

    } else if (ChallengeType.AUTHENTICATION == input.getType()) {
      var builder = StartAssertionOptions.builder()
        .userVerification(UserVerificationRequirement.PREFERRED);
      Optional.ofNullable(input.getUsername()).ifPresent(builder::username);

      AssertionRequest request = config.getRelyingParty().startAssertion(builder.build());

      input.setPayload(objectMapper.writeValueAsString(request));
      input.setOptions(objectMapper.readValue(
        objectMapper.writeValueAsString(request.getPublicKeyCredentialRequestOptions()), Map.class));
      input._getModelMetadata().getTouched().add("payload");

    } else {
      throw new BadRequestException("Invalid challenge type: " + input.getType());
    }
  }
}
