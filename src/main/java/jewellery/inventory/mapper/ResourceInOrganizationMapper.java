package jewellery.inventory.mapper;

import java.util.List;
import jewellery.inventory.dto.response.ResourceQuantityResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceInOrganizationMapper {

  private final OrganizationMapper organizationMapper;
  private final ResourceMapper resourceMapper;

  public ResourcesInOrganizationResponseDto toResourcesInOrganizationResponse(
      ResourceInOrganization resourceInOrganization) {
    if (resourceInOrganization != null) {
      return ResourcesInOrganizationResponseDto.builder()
          .owner(organizationMapper.toResponse(resourceInOrganization.getOrganization()))
          .dealPrice(resourceInOrganization.getDealPrice())
          .resourcesAndQuantities(List.of(getResourceQuantityResponseDto(resourceInOrganization)))
          .build();
    }
    return new ResourcesInOrganizationResponseDto();
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
