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

  public ResourcesInOrganizationResponseDto toResourceInOrganizationResponse(
      ResourceInOrganization resourceInOrganization) {
    return ResourcesInOrganizationResponseDto.builder()
        .owner(organizationMapper.toResponse(resourceInOrganization.getOrganization()))
        .dealPrice(resourceInOrganization.getDealPrice())
        .resourcesAndQuantities(List.of(getResourceQuantityResponseDto(resourceInOrganization)))
        .build();
  }

  public ResourcesInOrganizationResponseDto toResourceInOrganizationResponse(
      Organization organization) {
    List<ResourceQuantityResponseDto> resourceQuantityResponse =
        organization.getResourceInOrganization().stream()
            .map(this::getResourceQuantityResponseDto)
            .toList();
    return ResourcesInOrganizationResponseDto.builder()
        .resourcesAndQuantities(resourceQuantityResponse)
        .owner(organizationMapper.toResponse(organization))
        .build();
  }

  private ResourceQuantityResponseDto getResourceQuantityResponseDto(
      ResourceInOrganization resourceInOrganization) {
    return ResourceQuantityResponseDto.builder()
        .resource(resourceMapper.toResourceResponse(resourceInOrganization.getResource()))
        .quantity(resourceInOrganization.getQuantity())
        .build();
  }
}
