package org.apized.auth.api.user.verification;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.auth.api.user.UserService;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.error.exception.NotFoundException;
import org.apized.core.error.exception.UnauthorizedException;

import jakarta.transaction.Transactional;
import java.util.UUID;

@Introspected
@Transactional
@Controller("/users/{username}/verification")
public class VerificationController {
  @Inject
  UserRepository userRepository;

  @Operation(operationId = "Verify email", summary = "Verify email", tags = {"User"}, description = """
      
    """)
  @Post
  public HttpResponse verify(@PathVariable("username") String username, @Body VerificationRequest verificationRequest) {
    try {
      User user = userRepository.findByUsername(username).orElseThrow(NotFoundException::new);;
      if (!verificationRequest.getCode().equals(user.getEmailVerificationCode())) {
        throw new BadRequestException("Invalid code");
      }
      user.setVerified(true);
      user.setEmailVerificationCode(null);
      userRepository.update(user);
    } catch (Throwable t) {
      throw new UnauthorizedException("Not authorized");
    }
    return HttpResponse.accepted();
  }
}
