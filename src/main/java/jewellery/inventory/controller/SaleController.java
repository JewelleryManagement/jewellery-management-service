package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import jakarta.validation.Valid;
import jewellery.inventory.dto.request.SaleRequestDto;
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

  @Operation(summary = "Get all sales")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public List<SaleResponseDto> getAllSales() {
    return saleService.getAllSales();
  }

  @Operation(summary = "Create a sale")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public SaleResponseDto createSale(@RequestBody @Valid SaleRequestDto saleRequestDto) {
    return saleService.createSale(saleRequestDto);
  }
}
