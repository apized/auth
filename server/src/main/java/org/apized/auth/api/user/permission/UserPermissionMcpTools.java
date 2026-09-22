package org.apized.auth.api.user.permission;

import io.micronaut.mcp.annotations.Tool;
import io.micronaut.mcp.annotations.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.micronaut.mcp.McpContextInitializer;

import java.util.Map;
import java.util.UUID;

@Singleton
public class UserPermissionMcpTools {

  @Inject
  UserPermissionService permissionService;

  @Inject
  McpContextInitializer contextInitializer;

  @Tool(name = "user_grant_permission", description = "Grant a permission to a User")
  public Map<String, String> grantPermission(
    @ToolArg(name = "userId", description = "UUID of the User receiving the permission") String userId,
    @ToolArg(name = "permission", description = "Permission to grant") String permission
  ) {
    initializeContext();
    permissionService.grantPermissionTo(UUID.fromString(userId), permission);
    return Map.of("userId", userId, "permission", permission, "action", "granted");
  }

  @Tool(name = "user_revoke_permission", description = "Revoke a permission from a User")
  public Map<String, String> revokePermission(
    @ToolArg(name = "userId", description = "UUID of the User losing the permission") String userId,
    @ToolArg(name = "permission", description = "Permission to revoke") String permission
  ) {
    initializeContext();
    permissionService.revokePermissionFrom(UUID.fromString(userId), permission);
    return Map.of("userId", userId, "permission", permission, "action", "revoked");
  }

  private void initializeContext() {
    if (contextInitializer != null) contextInitializer.init();
  }
}
