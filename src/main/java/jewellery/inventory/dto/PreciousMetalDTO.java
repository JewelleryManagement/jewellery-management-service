package jewellery.inventory.dto;

import lombok.Data;

@Data
public class PreciousMetalDTO extends ResourceDTO{
  private String name;
  private String quantityType;
  private String type;
  private int purity;
  private String color;
  private String plating;
}
