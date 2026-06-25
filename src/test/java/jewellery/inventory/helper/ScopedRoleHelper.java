package jewellery.inventory.helper;

import java.util.*;
import java.util.stream.Collectors;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.dto.response.PermissionResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.PermissionScope;
import jewellery.inventory.model.RoleType;
import jewellery.inventory.model.ScopedRole;

public class ScopedRoleHelper {
  public static ScopedRoleRequestDto createOrganizationRoleRequest() {
    Set<Permission> permissions =
        Arrays.stream(Permission.values())
            .filter(permission -> permission.getPermissionScope() == PermissionScope.ORGANIZATION)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
    return new ScopedRoleRequestDto("ORGANIZATION_ADMIN", RoleType.ORGANIZATION, permissions);
  }

  public static ScopedRoleRequestDto createSystemRoleRequest() {
    Set<Permission> permissions =
        Arrays.stream(Permission.values())
            .filter(permission -> permission.getPermissionScope() == PermissionScope.SYSTEM)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
    return new ScopedRoleRequestDto("SYSTEM_ADMIN", RoleType.SYSTEM, permissions);
  }

  public static ScopedRole createRole(ScopedRoleRequestDto scopedRoleRequestDto) {
    return new ScopedRole(
        UUID.randomUUID(),
        scopedRoleRequestDto.getName(),
        scopedRoleRequestDto.getRoleType(),
        scopedRoleRequestDto.getPermissions());
  }

  public static ScopedRoleResponseDto createRoleResponse(ScopedRole scopedRole) {
    Set<PermissionResponseDto> permissions =
        scopedRole.getPermissions().stream()
            .map(ScopedRoleHelper::createPermissionResponse)
            .collect(Collectors.toSet());

    return new ScopedRoleResponseDto(
        scopedRole.getId(), scopedRole.getName(), scopedRole.getRoleType(), permissions);
  }

  private static PermissionResponseDto createPermissionResponse(Permission permission) {
    return new PermissionResponseDto(permission, permission.resolveIncludedPermissions());
  }

  public static Set<Permission> extractPermissions(ScopedRoleResponseDto response) {
    if (response.getPermissions() == null) {
      return Collections.emptySet();
    }

    return response.getPermissions().stream()
        .map(PermissionResponseDto::getPermission)
        .collect(Collectors.toSet());
  }
}
