package jewellery.inventory.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceInUserRequestDto {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 1.";
  private static final String QUANTITY_DECIMAL_PLACES_MSG =
      "Quantity should not have more than 2 decimal places.";
  private static final String PRICE_MIN_VALUE_MSG = "Price should not be negative number";
  private static final String PRICE_DECIMAL_PLACES_MSG =
          "Price should not have more than 2 decimal places.";

  @NotNull private UUID userId;
  @NotNull private UUID resourceId;

  @Min(value = 0, message = QUANTITY_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
  private double quantity;
  @NotNull
  @Min(value = 0, message = PRICE_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = PRICE_DECIMAL_PLACES_MSG)
  private double dealPrice;
}
