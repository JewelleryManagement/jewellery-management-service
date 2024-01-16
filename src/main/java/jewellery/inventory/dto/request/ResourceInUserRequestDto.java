package jewellery.inventory.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
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
  @NotNull private UUID userId;
  @NotNull private UUID resourceId;

  @Positive
  private BigDecimal quantity;
}
