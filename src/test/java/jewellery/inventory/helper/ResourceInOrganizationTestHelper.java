package jewellery.inventory.helper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.OrganizationQuantityResponseDto;
import jewellery.inventory.dto.response.ResourceOwnedByOrganizationsResponseDto;
import jewellery.inventory.model.Organization;
import jewellery.inventory.model.ResourceInOrganization;
import jewellery.inventory.model.resource.Resource;
import org.jetbrains.annotations.NotNull;

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

  public static TransferResourceRequestDto createTransferResourceRequestDto(
      UUID previousOwnerId, UUID newOwnerId, UUID resourceId, BigDecimal quantity) {
    TransferResourceRequestDto transferResourceRequestDto = new TransferResourceRequestDto();
    transferResourceRequestDto.setPreviousOwnerId(previousOwnerId);
    transferResourceRequestDto.setNewOwnerId(newOwnerId);
    transferResourceRequestDto.setTransferredResourceId(resourceId);
    transferResourceRequestDto.setQuantity(quantity);
    return transferResourceRequestDto;
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

  public static ResourceOwnedByOrganizationsResponseDto getResourceOwnedByOrganizationsResponseDto(Organization organization) {
    ResourceOwnedByOrganizationsResponseDto response = new ResourceOwnedByOrganizationsResponseDto();
    response.setResource(ResourceTestHelper.getPearlResponseDto());
    OrganizationQuantityResponseDto organizationQuantityResponseDto = new OrganizationQuantityResponseDto();
    organizationQuantityResponseDto.setOwner(OrganizationTestHelper.getTestOrganizationResponseDto(organization));
    organizationQuantityResponseDto.setQuantity(BigDecimal.TEN);
    response.setOrganizationsAndQuantities(List.of(organizationQuantityResponseDto));
    return response;
  }
}
