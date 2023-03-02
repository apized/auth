package org.apized.auth.api.user.permission;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apized.auth.api.user.User;
import org.apized.auth.api.user.UserService;
import org.apized.auth.security.AuthConverter;
import org.apized.core.context.ApizedContext;
import org.apized.core.error.exception.ForbiddenException;

import java.util.UUID;

@Singleton
public class UserPermissionService {
  @Inject
  UserService userService;

  public void grantPermissionTo(UUID id, String permission) {
    ensureUserContextPermissions(permission, "grant");
    addPermissionTo(userService.get(id), permission);
  }

  public void revokePermissionFrom(UUID id, String permission) {
    ensureUserContextPermissions(permission, "revoke");
    removePermissionFrom(userService.get(id), permission);
  }

  private void addPermissionTo(User instance, String permission) {
    if (!AuthConverter.convertAuthUserToApizedUser(instance).isAllowed(permission)) {
      instance.getPermissions().add(permission);
      userService.update(instance.getId(), instance);
    }
  }

  private void removePermissionFrom(User instance, String permission) throws ForbiddenException {
    instance.getPermissions().removeIf(it -> it.equals(permission) || it.startsWith(permission + "."));
    userService.update(instance.getId(), instance);
  }

  private void ensureUserContextPermissions(String permission, String action) throws ForbiddenException {
    if (!ApizedContext.getSecurity().getUser().isAllowed(permission)) {
      throw new ForbiddenException("Not allowed to " + action + " permission " + permission, permission);
    }
  }
}
