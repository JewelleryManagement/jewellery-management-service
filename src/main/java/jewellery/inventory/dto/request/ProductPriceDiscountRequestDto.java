package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
  @NotNull @Positive private Double salePrice;
  @NotNull @PositiveOrZero private Double discount;
}
