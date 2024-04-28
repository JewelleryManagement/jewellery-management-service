package jewellery.inventory.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchasedResourceQuantityRequestDto {

  @NotNull private ResourceQuantityRequestDto resourceAndQuantity;

  @NotNull
  @DecimalMin(value = "0")
  @DecimalMax(value = "100")
  private BigDecimal discount;
}
