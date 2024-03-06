package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ResourceInOrganizationRequestDto {
  @NotNull private UUID organizationId;
  @NotNull private UUID resourceId;
  @Positive private BigDecimal quantity;
  @Positive private BigDecimal dealPrice;
}
