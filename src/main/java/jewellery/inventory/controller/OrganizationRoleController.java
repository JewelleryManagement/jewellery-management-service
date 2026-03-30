package jewellery.inventory.controller;

import java.util.UUID;
import jewellery.inventory.dto.request.OrganizationRoleRequest;
import jewellery.inventory.service.OrganizationRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class OrganizationRoleController {
  private final OrganizationRoleService organizationRoleService;

  @PostMapping
  public ResponseEntity<Void> createRole(@RequestBody OrganizationRoleRequest request) {
    organizationRoleService.createRole(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{organizationId}/users/{userId}/roles/{roleId}")
  public ResponseEntity<Void> assignRole(
      @PathVariable UUID userId, @PathVariable UUID organizationId, @PathVariable UUID roleId) {
    organizationRoleService.assignRoleToUserInOrganization(userId, organizationId, roleId);
    return ResponseEntity.ok().build();
  }
}
