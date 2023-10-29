package jewellery.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import jewellery.inventory.dto.response.SaleResponseDto;
import jewellery.inventory.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
