package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import jewellery.inventory.dto.request.resource.ResourceQuantityRequestDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchasedResourceInUserRequestDto {

  @NotNull private ResourceQuantityRequestDto resource;

  @Positive private BigDecimal salePrice;

  @Positive private BigDecimal discount;
}
