package org.apized.auth.api.user.verification;

import io.micronaut.mcp.annotations.Tool;
import io.micronaut.mcp.annotations.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserController;
import org.apized.micronaut.mcp.McpContextInitializer;

import java.util.Map;
import java.util.UUID;

@Singleton
public class UserVerificationMcpTools {

  @Inject
  UserController userController;

  @Inject
  McpContextInitializer contextInitializer;

  @Tool(
    name = "user_verify",
    description = "Administratively verify a User without a verification code. Requires permission to update the User's verified field."
  )
  public Map<String, String> verify(
    @ToolArg(name = "userId", description = "UUID of the User to verify") String userId
  ) {
    if (contextInitializer != null) contextInitializer.init();

    User input = new User();
    input.setVerified(true);
    input._getModelMetadata().getTouched().add("verified");
    userController.update(UUID.fromString(userId), input);

    return Map.of("userId", userId, "verified", "true");
  }
}
