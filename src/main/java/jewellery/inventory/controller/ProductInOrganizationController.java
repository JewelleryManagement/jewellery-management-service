package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import jewellery.inventory.dto.request.ProductRequestDto;
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
  @Operation(summary = "Create a new product in organization")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/products")
  public ProductsInOrganizationResponseDto createProductInOrganization(@RequestBody @Valid ProductRequestDto productRequestDto) {
    return productInOrganizationService.createProductInOrganization(productRequestDto);
  }
  @Operation(summary = "Delete a new product in organization")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{organizationId}/products/{productId}")
  public void deleteProductInOrganization(@PathVariable UUID organizationId,@PathVariable UUID productId) {
     productInOrganizationService.deleteProductInOrganization(organizationId,productId);
  }
}
