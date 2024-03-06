package jewellery.inventory.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ResourcesInOrganizationResponseDto {
  private OrganizationResponseDto owner;
  private List<ResourceQuantityResponseDto> resourcesAndQuantities;
  private BigDecimal dealPrice;
}
