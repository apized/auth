package org.apized.auth.api.role.permission;

import io.micronaut.mcp.annotations.Tool;
import io.micronaut.mcp.annotations.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.micronaut.mcp.McpContextInitializer;

import java.util.Map;
import java.util.UUID;

@Singleton
public class RolePermissionMcpTools {

  @Inject
  RolePermissionService permissionService;

  @Inject
  McpContextInitializer contextInitializer;

  @Tool(name = "role_grant_permission", description = "Grant a permission to a Role")
  public Map<String, String> grantPermission(
    @ToolArg(name = "roleId", description = "UUID of the Role receiving the permission") String roleId,
    @ToolArg(name = "permission", description = "Permission to grant") String permission
  ) {
    initializeContext();
    permissionService.grantPermissionTo(UUID.fromString(roleId), permission);
    return Map.of("roleId", roleId, "permission", permission, "action", "granted");
  }

  @Tool(name = "role_revoke_permission", description = "Revoke a permission from a Role")
  public Map<String, String> revokePermission(
    @ToolArg(name = "roleId", description = "UUID of the Role losing the permission") String roleId,
    @ToolArg(name = "permission", description = "Permission to revoke") String permission
  ) {
    initializeContext();
    permissionService.revokePermissionFrom(UUID.fromString(roleId), permission);
    return Map.of("roleId", roleId, "permission", permission, "action", "revoked");
  }

  private void initializeContext() {
    if (contextInitializer != null) contextInitializer.init();
  }
}
