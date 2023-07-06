package org.apized.auth.api.user.permission;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
  @Operation(operationId = "Grant permission", summary = "Grant permission", tags = {"User"}, security = @SecurityRequirement(name = "bearerAuth"), description = """
    
  """)
  public HttpResponse grantPermission(UUID userId, String permission) {
    permissionService.grantPermissionTo(userId, permission);
    return HttpResponse.accepted();
  }

  @Operation(operationId = "Revoke permission", summary = "Revoke permission", tags = {"User"}, security = @SecurityRequirement(name = "bearerAuth"), description = """
    
  """)
  @Delete("/{permission}")
  public HttpResponse revokePermission(UUID userId, String permission) {
    permissionService.revokePermissionFrom(userId, permission);
    return HttpResponse.accepted();
  }
}
