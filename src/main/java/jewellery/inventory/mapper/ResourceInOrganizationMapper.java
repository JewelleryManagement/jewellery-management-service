package jewellery.inventory.mapper;

import java.math.BigDecimal;
import java.util.List;

import jewellery.inventory.dto.response.OrganizationTransferResourceResponseDto;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.resource.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceInOrganizationMapper {

  private final OrganizationMapper organizationMapper;
  private final ResourceMapper resourceMapper;

  public OrganizationTransferResourceResponseDto getOrganizationTransferResourceResponseDto(
      Organization previousOwner, Organization newOwner, Resource resource, BigDecimal quantity) {

    return OrganizationTransferResourceResponseDto.builder()
        .previousOwner(organizationMapper.toResponse(previousOwner))
        .newOwner(organizationMapper.toResponse(newOwner))
        .transferredResource(
            ResourceQuantityResponseDto.builder()
                .resource(resourceMapper.toResourceResponse(resource))
                .quantity(quantity)
                .build())
        .build();
  }

  public ResourcesInOrganizationResponseDto toResourcesInOrganizationResponse(
      ResourceInOrganization resourceInOrganization) {
    if (resourceInOrganization != null) {
      return ResourcesInOrganizationResponseDto.builder()
          .owner(organizationMapper.toResponse(resourceInOrganization.getOrganization()))
          .dealPrice(resourceInOrganization.getDealPrice())
          .resourcesAndQuantities(List.of(getResourceQuantityResponseDto(resourceInOrganization)))
          .build();
    }
    return null;
  }

  public ResourcesInOrganizationResponseDto toResourcesInOrganizationResponse(
      Organization organization) {
    return ResourcesInOrganizationResponseDto.builder()
        .resourcesAndQuantities(getResourcesQuantitiesResponseDto(organization))
        .owner(organizationMapper.toResponse(organization))
        .build();
  }

  private List<ResourceQuantityResponseDto> getResourcesQuantitiesResponseDto(
      Organization organization) {
    return organization.getResourceInOrganization().stream()
        .map(this::getResourceQuantityResponseDto)
        .toList();
  }

  private ResourceQuantityResponseDto getResourceQuantityResponseDto(
      ResourceInOrganization resourceInOrganization) {
    return ResourceQuantityResponseDto.builder()
        .resource(resourceMapper.toResourceResponse(resourceInOrganization.getResource()))
        .quantity(resourceInOrganization.getQuantity())
        .build();
  }
}
