package org.apized.auth.api.user.verification;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserService;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.error.exception.UnauthorizedException;

import javax.transaction.Transactional;
import java.util.UUID;

@Introspected
@Transactional
@Controller("/users/{userId}/verification")
public class VerificationController {

  @Inject
  UserService userService;

  @Post
  public HttpResponse verify(UUID userId, VerificationRequest verificationRequest) {
    try {
      User user = userService.find(userId);
      if (!verificationRequest.getCode().equals(user.getEmailVerificationCode())) {
        throw new BadRequestException("Invalid code");
      }
      user.setVerified(true);
    } catch (Throwable t) {
      throw new UnauthorizedException("Not authorized");
    }
    return HttpResponse.accepted();
  }
}
