package jewellery.inventory.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class PurchasedResourceQuantityResponseDto {

  private ResourceQuantityResponseDto resourceAndQuantity;
  private BigDecimal salePrice;
  private BigDecimal discount;
}
