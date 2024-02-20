package jewellery.inventory.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import jewellery.inventory.dto.response.resource.ResourceResponseDto;
import lombok.Data;

@Data
public class ResourceInOrganizationResponseDto {
  private UUID id;
  private OrganizationResponseDto organization;
  private ResourceResponseDto resource;
  private BigDecimal quantity;
  private BigDecimal dealPrice;
}
