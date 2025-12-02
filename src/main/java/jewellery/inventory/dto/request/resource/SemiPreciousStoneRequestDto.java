package jewellery.inventory.dto.request.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class SemiPreciousStoneRequestDto extends ResourceRequestDto {
  private String type;
  private String size;
  private String quality;
  private String color;
  private String shape;
  private String shapeSpecification;
  private String colorHue;
}
