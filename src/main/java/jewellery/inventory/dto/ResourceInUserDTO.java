package jewellery.inventory.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInUserDTO {
  private UUID ownerId;
  private UUID resourceId;
  private double quantity;
}
