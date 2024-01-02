package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceQuantityRequestDto {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 0.";
  private static final String QUANTITY_DECIMAL_PLACES_MSG =
      "Quantity should not have more than 2 decimal places.";

  private UUID id;

  @Min(value = 0, message = QUANTITY_MIN_VALUE_MSG)
  @Digits(integer = 10, fraction = 2, message = QUANTITY_DECIMAL_PLACES_MSG)
  private BigDecimal quantity;
}
