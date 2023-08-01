package jewellery.inventory.dto.response.resource;

import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInUserResponseDto {
  private UUID resourceId;
  private String ownerName;
  private String resourceClazz;
  private double quantity;
}
