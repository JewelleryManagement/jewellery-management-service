package jewellery.inventory.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInProductDTO {
  private UUID resourceId;
  private double quantity;
}
