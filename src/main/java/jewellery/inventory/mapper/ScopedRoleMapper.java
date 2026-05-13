package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.response.PermissionResponseDto;
import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.ScopedRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScopedRoleMapper {

  @Mapping(target = "permissions", source = "permissions")
  ScopedRoleResponseDto toResponse(ScopedRole role);

  List<ScopedRoleResponseDto> toResponseList(List<ScopedRole> roles);

  default PermissionResponseDto map(Permission permission) {
    if (permission == null) {
      return null;
    }

    PermissionResponseDto dto = new PermissionResponseDto();
    dto.setPermission(permission);
    dto.setIncluded(permission.resolveIncludedPermissions());
    return dto;
  }
}
