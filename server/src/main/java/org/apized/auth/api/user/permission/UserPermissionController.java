package org.apized.auth.api.user.permission;

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
@Controller("/users/{userId}/permissions")
public class UserPermissionController {

  @Inject
  UserPermissionService permissionService;

  @Post("/{permission}")
  public HttpResponse grantPermission(UUID userId, String permission) throws Exception {
    permissionService.grantPermissionTo(userId, permission);
    return HttpResponse.accepted();
  }

  @Delete("/{permission}")
  public HttpResponse revokePermission(UUID userId, String permission) throws Exception {
    permissionService.revokePermissionFrom(userId, permission);
    return HttpResponse.accepted();
  }
}
