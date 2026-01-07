package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.service.OrganizationSaleService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations/sales")
@RequiredArgsConstructor
public class OrganizationSaleController {

  private final OrganizationSaleService organizationSaleService;

  @Operation(summary = "Create sale from organization to user")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public OrganizationSaleResponseDto createSale(@Valid @RequestBody SaleRequestDto saleRequestDto) {
    return organizationSaleService.createSale(saleRequestDto);
  }

  @Operation(summary = "Return of a sold product from user to organization")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/return-product/{productId}")
  public ProductReturnResponseDto returnProduct(@PathVariable("productId") UUID productId) {
    return organizationSaleService.returnProduct(productId);
  }

  @Operation(summary = "Return of a sold resource from user to organization")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{saleId}/return-resource/{resourceId}")
  public ResourceReturnResponseDto returnResource(
      @PathVariable("saleId") UUID saleId, @PathVariable("resourceId") UUID resourceId) {
    return organizationSaleService.returnResource(saleId, resourceId);
  }

  @Operation(summary = "Get all sales from organization to user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<OrganizationSaleResponseDto> getAllSales() {
    return organizationSaleService.getAllSales();
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @Operation(summary = "Get sale from organization to user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{saleId}")
  public OrganizationSaleResponseDto getSale(@PathVariable("saleId") UUID saleId) {
    return organizationSaleService.getSale(saleId);
  }
}
