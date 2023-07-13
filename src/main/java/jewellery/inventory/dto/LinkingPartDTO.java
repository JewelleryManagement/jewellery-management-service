package jewellery.inventory.dto;

import lombok.Data;

@Data
public class LinkingPartDTO extends ResourceDTO{
  private String name;
  private String quantityType;
  private String description;
}
