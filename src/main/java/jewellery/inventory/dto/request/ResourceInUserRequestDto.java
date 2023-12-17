package jewellery.inventory.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResourceInUserRequestDto {
  private static final String QUANTITY_MIN_VALUE_MSG = "Quantity should not be less than 0.01.";

  @NotNull private UUID userId;
  @NotNull private UUID resourceId;

  @DecimalMin(value = "0.01", message = QUANTITY_MIN_VALUE_MSG)
  private double quantity;
}
