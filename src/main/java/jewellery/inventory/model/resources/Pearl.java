package jewellery.inventory.model.resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pearl extends Resource {
  private String type;
  private double size;
  private String quality;
  private String color;
  private String shape;
}
