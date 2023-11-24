package jewellery.inventory.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SaleResponseDto {
  private UserResponseDto seller;
  private UserResponseDto buyer;
  private List<ProductResponseDto> products;
  private LocalDate date;
  private double totalPrice;
  private double totalDiscountedPrice;
  private double totalDiscount;
}
