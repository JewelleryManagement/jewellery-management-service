package jewellery.inventory.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class UserDTO {
  private UUID id;
  private String name;
  private String email;
  private List<ProductDTO> productsOwned;
  private List<ResourceInUserDTO> resourcesOwned;
}
