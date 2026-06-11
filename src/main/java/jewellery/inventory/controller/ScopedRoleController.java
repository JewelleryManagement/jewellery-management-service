package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.PermissionResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.service.ScopedRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class ScopedRoleController {
  private final ScopedRoleService scopedRoleService;

  @Operation(summary = "Create a new role")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_ROLE_CREATE')")
  @PostMapping
  public ScopedRoleResponseDto createRole(@RequestBody ScopedRoleRequestDto request) {
    return scopedRoleService.createRole(request);
  }

  @Operation(summary = "Delete a role")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_ROLE_DELETE')")
  @DeleteMapping("/{roleId}")
  public void deleteRole(@PathVariable UUID roleId) {
    scopedRoleService.deleteRole(roleId);
  }

  @Operation(summary = "Get role by id")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_ROLE_READ')")
  @GetMapping("/{roleId}")
  public ScopedRoleResponseDto getRole(@PathVariable UUID roleId) {
    return scopedRoleService.getRole(roleId);
  }

  @Operation(summary = "Get all user organization roles")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/organization/users/{userId}")
  public List<ScopedRoleResponseDto> getAllUserOrganizationRoles(@PathVariable UUID userId) {
    return scopedRoleService.getAllUserOrganizationRoles(userId);
  }

  @Operation(summary = "Get all user system roles")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_ROLE_READ')")
  @GetMapping("/system/users/{userId}")
  public List<ScopedRoleResponseDto> getAllUserSystemRoles(@PathVariable UUID userId) {
    return scopedRoleService.getAllUserSystemRoles(userId);
  }

  @Operation(summary = "Get all roles by type")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_ROLE_READ')")
  @GetMapping("/type/{roleType}")
  public List<ScopedRoleResponseDto> getRolesByType(@PathVariable RoleType roleType) {
    return scopedRoleService.getRolesByType(roleType);
  }

  @Operation(summary = "Get permissions")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@auth.hasSystemPermission('SYSTEM_ROLE_READ')")
  @GetMapping("/permissions")
  public Set<PermissionResponseDto> getPermissionsByRoleType(@RequestParam RoleType roleType) {
    return scopedRoleService.getPermissionsByRoleType(roleType);
  }

  @Operation(summary = "Get current user system permissions")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/current-user/system-permissions")
  public Set<PermissionResponseDto> getCurrentUserSystemPermissions() {
    return scopedRoleService.getCurrentUserSystemPermissions();
  }
}
