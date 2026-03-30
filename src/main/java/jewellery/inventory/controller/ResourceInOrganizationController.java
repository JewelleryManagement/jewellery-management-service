package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.request.TransferResourceRequestDto;
import jewellery.inventory.dto.response.OrganizationTransferResourceResponseDto;
import jewellery.inventory.dto.response.ResourceOwnedByOrganizationsResponseDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.service.ResourceInOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations/resources-availability")
@RequiredArgsConstructor
@Validated
public class ResourceInOrganizationController {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 0.";

  private final ResourceInOrganizationService resourceInOrganizationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@orgAuth.hasPermission(#request.organizationId, 'ORGANIZATION_RESOURCE_ADD')")
  @Operation(summary = "Add resource to organization")
  public ResourcesInOrganizationResponseDto addResourceToOrganization(
      @RequestBody @Valid ResourceInOrganizationRequestDto request) {
    return resourceInOrganizationService.addResourceToOrganization(request);
  }

  @Operation(
      summary =
          "Delete specific amount of resource from organization by organizationId, resourceId and quantity")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_RESOURCE_DELETE')")
  @DeleteMapping("/{organizationId}/{resourceId}/{quantity}")
  public ResourcesInOrganizationResponseDto removeQuantityFromOrganizationResource(
      @PathVariable UUID organizationId,
      @PathVariable UUID resourceId,
      @PathVariable("quantity") @PositiveOrZero(message = QUANTITY_MIN_VALUE_MSG)
          BigDecimal quantity) {
    return resourceInOrganizationService.removeQuantityFromResource(
        organizationId, resourceId, quantity);
  }

  @Operation(summary = "Get resources by organizationId")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("@orgAuth.hasPermission(#organizationId, 'ORGANIZATION_RESOURCE_READ')")
  @GetMapping("/{organizationId}")
  public ResourcesInOrganizationResponseDto getAllResourcesFromOrganization(
      @PathVariable UUID organizationId) {
    return resourceInOrganizationService.getAllResourcesFromOrganization(organizationId);
  }

  @Operation(summary = "Transfer resource from organization to another organization")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(
      "@orgAuth.hasPermission(#transferResourceRequestDto.previousOwnerId, 'ORGANIZATION_RESOURCE_TRANSFER') && "
          + "@orgAuth.hasPermission(#transferResourceRequestDto.newOwnerId, 'ORGANIZATION_RESOURCE_TRANSFER')")
  @PostMapping("/transfer")
  public OrganizationTransferResourceResponseDto transferResources(
      @RequestBody @Valid TransferResourceRequestDto transferResourceRequestDto) {
    return resourceInOrganizationService.transferResource(transferResourceRequestDto);
  }

  @Operation(summary = "Get all resources quantities from organizations by resourceId")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/by-resource/{resourceId}")
  public ResourceOwnedByOrganizationsResponseDto getAllOrganizationsAndQuantitiesByResource(
      @PathVariable UUID resourceId) {
    return resourceInOrganizationService.getOrganizationsAndQuantities(resourceId);
  }
}
