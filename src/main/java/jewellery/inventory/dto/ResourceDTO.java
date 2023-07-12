package jewellery.inventory.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ResourceDTO {
  private UUID id;
  private String name;
  private String quantityType;
  private List<ResourceInUserDTO> userAffiliations;
  private List<ResourceInProductDTO> productAffiliations;
}
