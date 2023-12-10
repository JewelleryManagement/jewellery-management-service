package jewellery.inventory.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourcePurchaseRequestDto {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 1.";
  private static final String QUANTITY_DECIMAL_PLACES_MSG =
      "Quantity should not have more than 2 decimal places.";
  private static final String DEAL_PRICE_MIN_VALUE_MSG = "Deal price should not be less than 0.";
  private static final String DEAL_PRICE_DECIMAL_PLACES_MSG =
      "Deal price should not have more than 2 decimal places.";

  @NotNull private UUID userId;
  @NotNull private UUID resourceId;

  @Min(value = 0, message = QUANTITY_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
  private double quantity;

  @NotNull
  @Min(value = 0, message = QUANTITY_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
  private double dealPrice;
}
