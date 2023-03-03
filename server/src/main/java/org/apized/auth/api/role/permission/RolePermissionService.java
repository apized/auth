package org.apized.auth.api.role.permission;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.role.Role;
import org.apized.auth.api.role.RoleService;
import org.apized.auth.security.AuthConverter;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.ForbiddenException;

import java.util.UUID;

@Singleton
public class RolePermissionService {
  @Inject
  RoleService roleService;

  public void grantPermissionTo(UUID id, String permission) {
    ensureUserContextPermissions(permission, "grant");
    addPermissionTo(roleService.get(id), permission);
  }

  public void revokePermissionFrom(UUID id, String permission) {
    ensureUserContextPermissions(permission, "revoke");
    removePermissionFrom(roleService.get(id), permission);
  }

  private void addPermissionTo(Role instance, String permission) {
    if (!AuthConverter.convertAuthRoleToApizedRole(instance).isAllowed(permission)) {
      instance.getPermissions().add(permission);
      roleService.update(instance.getId(), instance);
    }
  }

  private void removePermissionFrom(Role instance, String permission) throws ForbiddenException {
    instance.getPermissions().removeIf(it -> it.equals(permission) || it.startsWith(permission + "."));
    roleService.update(instance.getId(), instance);
  }

  private void ensureUserContextPermissions(String permission, String action) throws ForbiddenException {
    if (!ApizedContext.getSecurity().getUser().isAllowed(permission)) {
      throw new ForbiddenException("Not allowed to " + action + " permission " + permission, permission);
    }
  }
}
