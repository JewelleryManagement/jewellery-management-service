package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ScopedRoleResponseDto;
import jewellery.inventory.model.ScopedRole;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScopedRoleMapper {
  ScopedRoleResponseDto toResponse(ScopedRole role);
}
