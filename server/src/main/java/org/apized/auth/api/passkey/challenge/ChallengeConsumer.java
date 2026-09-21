package org.apized.auth.api.passkey.challenge;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.apized.core.error.exception.BadRequestException;

import java.util.UUID;

@Singleton
public class ChallengeConsumer {
  @Inject
  ChallengeService challengeService;

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public Challenge consume(UUID challengeId, ChallengeType expectedType, UUID expectedUserId) {
    Challenge challenge = challengeService.get(challengeId);

    if (expectedType != challenge.getType()
      || expectedUserId != null && !expectedUserId.equals(challenge.getUserId())) {
      throw new BadRequestException("Invalid challenge");
    }

    challengeService.delete(challengeId);
    return challenge;
  }
}
