package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jewellery.inventory.dto.request.SaleRequestDto;
import jewellery.inventory.dto.response.OrganizationSaleResponseDto;
import jewellery.inventory.service.SaleFromOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationSaleController {

  private final SaleFromOrganizationService saleFromOrganizationService;

  @Operation(summary = "Create sale from organization to user")
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/sales")
  public OrganizationSaleResponseDto createSale(@Valid @RequestBody SaleRequestDto saleRequestDto) {
    return saleFromOrganizationService.createSale(saleRequestDto);
  }
}
