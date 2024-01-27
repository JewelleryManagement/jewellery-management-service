package jewellery.inventory.dto.response;

import java.math.BigDecimal;
import jewellery.inventory.dto.response.resource.ResourceQuantityResponseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
public class PurchasedResourceInUserResponseDto {

  private ResourceQuantityResponseDto resource;
  private UserResponseDto owner;
  private BigDecimal salePrice;
  private BigDecimal discount;
}
