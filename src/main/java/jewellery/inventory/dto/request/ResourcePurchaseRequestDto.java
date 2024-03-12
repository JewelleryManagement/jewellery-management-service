package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResourcePurchaseRequestDto extends ResourceInUserRequestDto {

  @NotNull
  @Positive
  private BigDecimal dealPrice;
}
