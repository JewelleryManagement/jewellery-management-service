package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.model.Organization;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    OrganizationResponseDto toResponse(Organization organization);
}
