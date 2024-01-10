package jewellery.inventory.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResourcePurchaseRequestDto extends ResourceInUserRequestDto {
 
  private static final String DEAL_PRICE_MIN_VALUE_MSG = "Deal price should not be less than 0.";
  private static final String DEAL_PRICE_DECIMAL_PLACES_MSG =
      "Deal price should not have more than 2 decimal places.";

  @NotNull
  @Min(value = 0, message = DEAL_PRICE_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = DEAL_PRICE_DECIMAL_PLACES_MSG)
  private double dealPrice;
}
