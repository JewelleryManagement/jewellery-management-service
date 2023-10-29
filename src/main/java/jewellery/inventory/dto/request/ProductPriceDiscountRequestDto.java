package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductPriceDiscountRequestDto {
  @NotNull private UUID productId;
  @NotNull private double salePrice;
  @NotNull private double discount;
}
