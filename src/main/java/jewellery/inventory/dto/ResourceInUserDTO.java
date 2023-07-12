package jewellery.inventory.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInUserDTO {
  private UUID id;
  private UserDTO owner;
  private ResourceDTO resource;
  private double quantity;
}
