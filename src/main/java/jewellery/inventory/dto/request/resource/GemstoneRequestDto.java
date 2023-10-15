package jewellery.inventory.dto.request.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class GemstoneRequestDto extends ResourceRequestDto {
  private String color;
  private double carat;
  private String cut;
  private String clarity;
  private double dimensionX;
  private double dimensionY;
  private double dimensionZ;
  private String size;
  private String shape;
}
