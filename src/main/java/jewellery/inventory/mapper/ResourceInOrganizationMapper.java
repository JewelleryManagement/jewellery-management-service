package jewellery.inventory.mapper;

import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ResourceInOrganizationMapper {
    ResourceInOrganizationMapper INSTANCE = Mappers.getMapper(ResourceInOrganizationMapper.class);

    @Mapping(source = "organization", target = "owner")
    @Mapping(source = "resourceInOrganization", target = "resourcesAndQuantities")
    ResourcesInOrganizationResponseDto toResourceInOrganizationResponseDto(Organization organization);

    OrganizationResponseDto toOrganizationResponseDto(Organization organization);

    ResourceQuantityResponseDto toResourceQuantityResponseDto(ResourceInOrganization resource);

    default List<ResourceQuantityResponseDto> toQuantities(List<ResourceInOrganization> resources) {
        return resources.stream()
                .map(this::toResourceQuantityResponseDto)
                .collect(Collectors.toList());
    }
}