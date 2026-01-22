package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.service.SaleService;
import jewellery.inventory.utils.NotUsedYet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

  private final SaleService saleService;

  @Operation(summary = "Create sale from organization to user")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public OrganizationSaleResponseDto createSale(@Valid @RequestBody SaleRequestDto saleRequestDto) {
    return saleService.createSale(saleRequestDto);
  }

  @Operation(summary = "Return of a sold product from user to organization")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/return-product/{productId}")
  public ProductReturnResponseDto returnProduct(@PathVariable("productId") UUID productId) {
    return saleService.returnProduct(productId);
  }

  @Operation(summary = "Return of a sold resource from user to organization")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{saleId}/return-resource/{resourceId}")
  public ResourceReturnResponseDto returnResource(
      @PathVariable("saleId") UUID saleId, @PathVariable("resourceId") UUID resourceId) {
    return saleService.returnResource(saleId, resourceId);
  }

  @Operation(summary = "Get all sales from organization to user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<OrganizationSaleResponseDto> getAllSales() {
    return saleService.getAllSales();
  }

  @NotUsedYet(reason = "Pending frontend implementation")
  @Operation(summary = "Get sale from organization to user")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{saleId}")
  public OrganizationSaleResponseDto getSale(@PathVariable("saleId") UUID saleId) {
    return saleService.getSale(saleId);
  }

  @Operation(summary = "Get all sales for resource")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/resource/{resourceId}")
  public List<OrganizationSaleResponseDto> getAllSalesByResource(
      @PathVariable("resourceId") UUID resourceId) {
    return saleService.getAllSalesByResource(resourceId);
  }
}
