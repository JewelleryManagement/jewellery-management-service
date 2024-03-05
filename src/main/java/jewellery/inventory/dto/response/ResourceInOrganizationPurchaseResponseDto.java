package jewellery.inventory.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInOrganizationPurchaseResponseDto extends ResourcesInOrganizationResponseDto {
  private BigDecimal dealPrice;
}
