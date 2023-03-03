package org.apized.auth.api.role.permission;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import javax.transaction.Transactional;
import java.util.UUID;

@Introspected
@Transactional
@Controller("/roles/{roleId}/permissions")
public class RolePermissionController {

  @Inject
  RolePermissionService permissionService;

  @Post("/{permission}")
  public HttpResponse grantPermission(UUID roleId, String permission) {
    permissionService.grantPermissionTo(roleId, permission);
    return HttpResponse.accepted();
  }

  @Delete("/{permission}")
  public HttpResponse revokePermission(UUID roleId, String permission) {
    permissionService.revokePermissionFrom(roleId, permission);
    return HttpResponse.accepted();
  }
}
