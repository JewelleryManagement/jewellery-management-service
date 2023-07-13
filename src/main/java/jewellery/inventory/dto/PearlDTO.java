package jewellery.inventory.dto;

import lombok.Data;

@Data
public class PearlDTO extends ResourceDTO{
  private String name;
  private String quantityType;
  private String type;
  private double size;
  private String quality;
  private String color;
  private String shape;
}
