package org.apized.auth.api.passkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.apized.auth.api.passkey.challenge.Challenge;
import org.apized.auth.api.passkey.challenge.ChallengeService;
import org.apized.auth.api.passkey.challenge.ChallengeType;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.auth.passkey.PasskeyConfig;
import org.apized.auth.passkey.PasskeyCredentialService;
import org.apized.core.behaviour.BehaviourHandler;
import org.apized.core.behaviour.annotation.Behaviour;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.error.exception.UnauthorizedException;
import org.apized.core.execution.Execution;
import org.apized.core.model.Action;
import org.apized.core.model.Layer;
import org.apized.core.model.When;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Behaviour(model = Passkey.class, layer = Layer.SERVICE, when = When.BEFORE, actions = Action.CREATE)
public class PasskeyBehaviour implements BehaviourHandler<Passkey> {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  PasskeyConfig config;

  @Inject
  ChallengeService challengeService;

  @Inject
  PasskeyRepository passkeyRepository;

  @Inject
  UserRepository userRepository;

  @Override
  @SneakyThrows
  public void preCreate(Execution execution, Passkey input) {
    User user = userRepository.get(ApizedContext.getSecurity().getUser().getId()).get();

    Challenge challenge = challengeService.get(input.getChallengeId());

    if (ChallengeType.REGISTRATION == challenge.getType() || !user.getId().equals(challenge.getUserId())) {
      throw new BadRequestException("Invalid challenge");
    }

    PublicKeyCredentialCreationOptions options =
      objectMapper.readValue(challenge.getPayload(), PublicKeyCredentialCreationOptions.class);

    RegistrationResult result = config.getRelyingParty().finishRegistration(
      FinishRegistrationOptions.builder()
        .request(options)
        .response(PublicKeyCredential.parseRegistrationResponseJson(
          objectMapper.writeValueAsString(input.getCredential())
        ))
        .build()
    );

    challengeService.delete(challenge.getId());

    input.setUser(user);
    input.setCredentialId(result.getKeyId().getId().getBase64Url());
    input.setPublicKeyCose(result.getPublicKeyCose().getBytes());
    input.setSignCount(result.getSignatureCount());
    input.setAaguid(result.getAaguid() != null ? result.getAaguid().getHex() : null);
    input.setTransports(result.getKeyId().getTransports()
      .map(t -> t.stream().map(AuthenticatorTransport::getId).collect(Collectors.joining(",")))
      .orElse(""));
    input._getModelMetadata().getTouched().addAll(List.of(
      "userId", "credentialId", "publicKeyCose", "signCount", "aaguid", "transports"
    ));
  }

  @SneakyThrows
  public String toJson(Object value) {
    return objectMapper.writeValueAsString(value);
  }

  @SneakyThrows
  public User finishAuthentication(UUID challengeId, String credentialJson) {
    Challenge challenge = challengeService.get(challengeId);

    AssertionRequest request = objectMapper.readValue(challenge.getPayload(), AssertionRequest.class);

    AssertionResult result = config.getRelyingParty().finishAssertion(
      FinishAssertionOptions.builder()
        .request(request)
        .response(PublicKeyCredential.parseAssertionResponseJson(credentialJson))
        .build()
    );

    if (!result.isSuccess()) {
      throw new UnauthorizedException("Passkey authentication failed");
    }

    challengeService.delete(challengeId);

    passkeyRepository.findByCredentialId(result.getCredential().getCredentialId().getBase64Url()).ifPresent(passkey -> {
      passkey.setSignCount(result.getSignatureCount());
      passkeyRepository.update(passkey);
    });

    UUID userId = PasskeyCredentialService.byteArrayToUuid(result.getCredential().getUserHandle());
    return userRepository.get(userId)
      .orElseThrow(() -> new UnauthorizedException("User not found"));
  }
}
