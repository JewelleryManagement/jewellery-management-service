package jewellery.inventory.dto.response.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class SemiPreciousStoneResponseDto extends ResourceResponseDto {
  private String type;
  private String size;
  private String quality;
  private String color;
  private String shape;
  private String shapeSpecification;
  private String colorHue;
}
