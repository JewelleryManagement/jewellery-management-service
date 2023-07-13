package jewellery.inventory.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class GemstoneDTO extends ResourceDTO{
  private String color;
  private double carat;
  private String cut;
  private String clarity;
  private double dimensionX;
  private double dimensionY;
  private double dimensionZ;
  private String shape;
}
