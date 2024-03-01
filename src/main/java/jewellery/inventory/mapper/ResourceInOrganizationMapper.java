package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.ResourceInOrganizationResponseDto;
import jewellery.inventory.model.ResourceInOrganization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResourceInOrganizationMapper {
    @Mapping(source = "organization", target = "owner")
    @Mapping(source = "resource", target = "resourcesAndQuantities.resource")
    @Mapping(source = "quantity", target = "resourcesAndQuantities.quantity")
    ResourceInOrganizationResponseDto toResourceInOrganizationResponse(ResourceInOrganization resourceInOrganization);
}
