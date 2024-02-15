package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchasedResourceQuantityRequestDto {

  @NotNull private ResourceQuantityRequestDto resourceAndQuantity;

  @Positive private BigDecimal discount;
}
