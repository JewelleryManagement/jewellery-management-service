package jewellery.inventory.model.resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreciousMetal extends Resource {
  private String type;
  private int purity;
  private String color;
  private String plating;
}
