package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.service.ScopedRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class ScopedRoleController {
  private final ScopedRoleService scopedRoleService;

  @Operation(summary = "Create a new role")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ScopedRoleResponseDto createRole(@RequestBody ScopedRoleRequestDto request) {
    return scopedRoleService.createRole(request);
  }

  @Operation(summary = "Delete a role")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{roleId}")
  public void deleteRole(@PathVariable UUID roleId) {
    scopedRoleService.deleteRole(roleId);
  }

  @Operation(summary = "Get role by id")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{roleId}")
  public ScopedRoleResponseDto getRole(@PathVariable UUID roleId) {
    return scopedRoleService.getRole(roleId);
  }

  @Operation(summary = "Get all roles")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping()
  public List<ScopedRoleResponseDto> getAllRoles() {
    return scopedRoleService.getAllRoles();
  }

  @Operation(summary = "Get all user roles")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/users/{userId}")
  public List<ScopedRoleResponseDto> getAllUserRoles(@PathVariable UUID userId) {
    return scopedRoleService.getAllUserRoles(userId);
  }

  @Operation(summary = "Get all user roles for organization")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/organizations/{organizationId}/users/{userId}")
  public List<ScopedRoleResponseDto> getAllUserRoles(
      @PathVariable UUID organizationId, @PathVariable UUID userId) {
    return scopedRoleService.getAllUserRolesByOrganization(userId, organizationId);
  }
}
