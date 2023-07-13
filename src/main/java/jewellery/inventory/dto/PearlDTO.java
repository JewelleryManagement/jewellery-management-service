package jewellery.inventory.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class PearlDTO extends ResourceDTO{
  private String type;
  private double size;
  private String quality;
  private String color;
  private String shape;
}
