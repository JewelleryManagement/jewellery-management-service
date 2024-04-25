package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.service.OrganizationSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationSaleController {

  private final OrganizationSaleService organizationSaleService;

  @Operation(summary = "Create sale from organization to user")
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sales")
  public OrganizationSaleResponseDto createSale(@Valid @RequestBody SaleRequestDto saleRequestDto) {
    return organizationSaleService.createSale(saleRequestDto);
  }

  @Operation(summary = "Return of a sold product from user to organization")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/return-product/{productId}")
  public ProductReturnResponseDto returnProduct(@PathVariable("productId") UUID productId) {
    return organizationSaleService.returnProduct(productId);
  }
}
