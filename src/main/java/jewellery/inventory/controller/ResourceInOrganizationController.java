package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.dto.request.ResourceInOrganizationRequestDto;
import jewellery.inventory.dto.response.ResourcesInOrganizationResponseDto;
import jewellery.inventory.service.ResourceInOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations/resources-availability")
@RequiredArgsConstructor
public class ResourceInOrganizationController {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 0.";

  private final ResourceInOrganizationService resourceInOrganizationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Add resource to organization")
  public ResourcesInOrganizationResponseDto addResourceToOrganization(
      @RequestBody @Valid ResourceInOrganizationRequestDto request) {
    return resourceInOrganizationService.addResourceToOrganization(request);
  }

  @Operation(
      summary =
          "Delete specific amount of resource from organization by organizationId, resourceId and quantity")
  @ResponseStatus(HttpStatus.OK)
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
  @GetMapping("/{organizationId}")
  public ResourcesInOrganizationResponseDto getAllResourcesFromUser(
      @PathVariable UUID organizationId) {
    return resourceInOrganizationService.getAllResourcesFromOrganization(organizationId);
  }
}
