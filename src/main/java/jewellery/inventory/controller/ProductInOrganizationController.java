package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import jewellery.inventory.dto.response.ProductsInOrganizationResponseDto;
import jewellery.inventory.service.ProductInOrganizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@AllArgsConstructor
public class ProductInOrganizationController {
  private final ProductInOrganizationService productInOrganizationService;

  @Operation(summary = "Get all products in organization")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{organizationId}/products")
  public ProductsInOrganizationResponseDto getAllProductsInOrganization(
      @PathVariable UUID organizationId) {
    return productInOrganizationService.getProductsInOrganization(organizationId);
  }
}
