package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchasedResourceInUserRequestDto {
  @NotNull private ResourceInUserRequestDto resources;

  @Positive private BigDecimal salePrice;

  @Positive private BigDecimal discount;
}
