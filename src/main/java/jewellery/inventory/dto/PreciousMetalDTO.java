package jewellery.inventory.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class PreciousMetalDTO extends ResourceDTO{
  private String type;
  private int purity;
  private String color;
  private String plating;
}
