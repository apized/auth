package org.apized.auth.api.user.password;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.auth.api.user.UserService;
import org.apized.auth.security.BCrypt;
import org.apized.auth.security.CodeGenerator;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.error.exception.NotFoundException;
import org.apized.core.error.exception.UnauthorizedException;

import javax.transaction.Transactional;

@Introspected
@Transactional
@Controller("/users/{username}/password")
public class PasswordResetController {

  @Inject
  UserRepository userRepository;

  @Post
  public HttpResponse setPassword(String username, PasswordResetRequest passwordResetRequest) {
    try {
      User user = userRepository.findByUsername(username).orElseThrow(NotFoundException::new);
      if (user.getPasswordResetCode().equals(passwordResetRequest.getCode())) {
        user.setPassword(BCrypt.hashpw(passwordResetRequest.getPassword(), BCrypt.gensalt()));
        user.setPasswordResetCode(null);
        userRepository.update(user);
      } else {
        throw new BadRequestException("Invalid code");
      }
    } catch (Throwable t) {
      throw new UnauthorizedException("Not authorized");
    }
    return HttpResponse.accepted();
  }

  @Delete
  @Post()
  public HttpResponse reset(String username) {
    try {
      User user = userRepository.findByUsername(username).orElseThrow(NotFoundException::new);
      user.setPasswordResetCode(CodeGenerator.generateCode(128));
      user._getModelMetadata().getTouched().add("passwordResetCode");
      userRepository.update(user);
      //todo send password reset email
    } catch (Throwable t) {
      //Do nothing. We don't want to expose a way for checking for valid usernames
    }
    return HttpResponse.accepted();
  }
}
