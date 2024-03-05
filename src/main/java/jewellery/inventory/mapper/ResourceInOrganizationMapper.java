package jewellery.inventory.mapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jewellery.inventory.dto.response.OrganizationResponseDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.resource.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ResourceInOrganizationMapper {
  ResourceInOrganizationMapper INSTANCE = Mappers.getMapper(ResourceInOrganizationMapper.class);

  @Mapping(source = "organization", target = "owner")
  @Mapping(source = "resourceInOrganization", target = "resourcesAndQuantities")
  ResourcesInOrganizationResponseDto toResourceInOrganizationResponseDto(Organization organization);

  OrganizationResponseDto toOrganizationResponseDto(Organization organization);

  ResourceQuantityResponseDto toResourceQuantityResponseDto(ResourceInOrganization resource);

  default List<ResourceQuantityResponseDto> toQuantities(List<ResourceInOrganization> resources) {
    return resources.stream().map(this::toResourceQuantityResponseDto).collect(Collectors.toList());
  }

  @Mapping(source = "organization", target = "owner")
  @Mapping(
      target = "resourcesAndQuantities",
      expression =
          "java(toResourceQuantityResponseList(resourceInOrganization.getResource(), resourceInOrganization.getQuantity()))")
  ResourcesInOrganizationResponseDto toResourceInOrganizationResponse(
      ResourceInOrganization resourceInOrganization);

  default List<ResourceQuantityResponseDto> toResourceQuantityResponseList(
      Resource resource, BigDecimal quantity) {
    ResourceResponseDto resourceResponseDto = toResourceResponseDto(resource);
    ResourceQuantityResponseDto resourceQuantityResponseDto =
        ResourceQuantityResponseDto.builder()
            .resource(resourceResponseDto)
            .quantity(quantity)
            .build();
    return Collections.singletonList(resourceQuantityResponseDto);
  }

  ResourceResponseDto toResourceResponseDto(Resource resource);
}
