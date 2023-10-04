package org.apized.auth.security;

import org.apized.auth.api.role.Role;
import org.apized.auth.api.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AuthConverter {
  public static org.apized.core.security.model.User convertAuthUserToApizedUser(User user) {
    return new org.apized.core.security.model.User(
      user.getId(),
      user.getUsername(),
      user.getName(),
      user.getRoles().stream().map(AuthConverter::convertAuthRoleToApizedRole).toList(),
      user.getPermissions(),
      new ArrayList<>(),
      Map.of()
    );
  }

  public static org.apized.core.security.model.Role convertAuthRoleToApizedRole(Role role) {
    return new org.apized.core.security.model.Role(
      role.getId(),
      role.getName(),
      role.getDescription(),
      role.getPermissions()
    );
  }
}
