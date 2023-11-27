package jewellery.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductReturnResponseDto {
  private ProductResponseDto returnedProduct;
  private SaleResponseDto saleAfter;
  private LocalDate date;
}
