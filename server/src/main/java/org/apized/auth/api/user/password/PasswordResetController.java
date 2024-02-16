package org.apized.auth.api.user.password;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserRepository;
import org.apized.auth.security.BCrypt;
import org.apized.auth.security.CodeGenerator;
import org.apized.core.error.exception.BadRequestException;
import org.apized.core.error.exception.NotFoundException;
import org.apized.core.error.exception.UnauthorizedException;

@Introspected
@Transactional
@Controller("/users/{username}/password")
public class PasswordResetController {
  @Inject
  UserRepository userRepository;

  @Operation(operationId = "Reset password confirmation", summary = "Reset password confirmation", tags = {"User"}, description = """
      
    """)
  @Post
  public HttpResponse setPassword(@PathVariable("username") String username, @Body PasswordResetRequest passwordResetRequest) {
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

  @Operation(operationId = "Reset password", summary = "Reset password", tags = {"User"}, description = """
      
    """)
  @Delete
  public HttpResponse reset(@PathVariable("username") String username) {
    try {
      User user = userRepository.findByUsername(username).orElseThrow(NotFoundException::new);
      user.setPasswordResetCode(CodeGenerator.generateCode());
      user._getModelMetadata().getTouched().add("passwordResetCode");
      userRepository.update(user);
    } catch (Throwable t) {
      //Do nothing. We don't want to expose a way for checking for valid usernames
    }
    return HttpResponse.accepted();
  }
}
