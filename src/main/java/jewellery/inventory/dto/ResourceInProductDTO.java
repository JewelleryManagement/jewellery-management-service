package jewellery.inventory.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class ResourceInProductDTO {
  private UUID id;
  private ResourceDTO resource;
  private double quantity;
  private ProductDTO product;
}
