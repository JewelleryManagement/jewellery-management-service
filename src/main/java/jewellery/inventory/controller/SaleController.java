package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.ProductReturnResponseDto;
import jewellery.inventory.dto.response.ResourceReturnResponseDto;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {
  private final SaleService saleService;

  @Operation(summary = "Create a sale")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public SaleResponseDto createSale(@Valid @RequestBody SaleRequestDto saleRequestDto) {
    return saleService.createSale(saleRequestDto);
  }

  @Operation(summary = "Return of a sold product")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/return-product/{productId}")
  public ProductReturnResponseDto returnProduct(@PathVariable("productId") UUID productId) {
    return saleService.returnProduct(productId);
  }

  @Operation(summary = "Return of a sold resource")
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{saleId}/return-resource/{resourceId}")
  public ResourceReturnResponseDto returnResource(
      @PathVariable("saleId") UUID saleId, @PathVariable("resourceId") UUID resourceId) {
    return saleService.returnResource(saleId, resourceId);
  }
}
