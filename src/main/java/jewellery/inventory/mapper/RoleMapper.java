package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.RoleResponseDto;
import jewellery.inventory.model.OrganizationRole;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
  RoleResponseDto toResponse(OrganizationRole role);
}
