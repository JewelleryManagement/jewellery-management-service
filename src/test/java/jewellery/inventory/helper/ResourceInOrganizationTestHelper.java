package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.resource.Resource;

public class ResourceInOrganizationTestHelper {

  public static ResourceInOrganizationRequestDto createResourceInOrganizationRequestDto(
      UUID organizationId, UUID resourceId, BigDecimal quantity, BigDecimal dealPrice) {
    return ResourceInOrganizationRequestDto.builder()
        .organizationId(organizationId)
        .resourceId(resourceId)
        .quantity(quantity)
        .dealPrice(dealPrice)
        .build();
  }

  public static ResourceInOrganization createResourceInOrganization(
      Organization organization, Resource resource) {
    return ResourceInOrganization.builder()
        .id(UUID.randomUUID())
        .organization(organization)
        .resource(resource)
        .quantity(BigDecimal.valueOf(100))
        .dealPrice(BigDecimal.ONE)
        .build();
  }
}
