package jewellery.inventory.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInUserRequestDto {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 1.";
  private static final String QUANTITY_DECIMAL_PLACES_MSG =
      "Quantity should not have more than 2 decimal places.";

  @NotNull private UUID userId;
  @NotNull private UUID resourceId;

  @Min(value = 0, message = QUANTITY_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
  private double quantity;
}
