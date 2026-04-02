package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.RoleRequestDto;
import jewellery.inventory.dto.response.RoleResponseDto;
import jewellery.inventory.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  @Operation(summary = "Create a new role")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public RoleResponseDto createRole(@RequestBody RoleRequestDto request) {
    return roleService.createRole(request);
  }

  @Operation(summary = "Delete a role")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{roleId}")
  public void deleteRole(@PathVariable UUID roleId) {
    roleService.deleteRole(roleId);
  }

  @Operation(summary = "Get role by id")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{roleId}")
  public RoleResponseDto getRole(@PathVariable UUID roleId) {
    return roleService.getRole(roleId);
  }

  @Operation(summary = "Get all roles")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping()
  public List<RoleResponseDto> getAllRoles() {
    return roleService.getAllRoles();
  }

  @Operation(summary = "Get all user roles")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/users/{userId}")
  public List<RoleResponseDto> getAllUserRoles(@PathVariable UUID userId) {
    return roleService.getAllUserRoles(userId);
  }

  @Operation(summary = "Get all user roles for organization")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/organizations/{organizationId}/users/{userId}")
  public List<RoleResponseDto> getAllUserRoles(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    return roleService.getAllUserRolesByOrganization(userId, organizationId);
  }
}
