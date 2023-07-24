package jewellery.inventory.dto.request.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class PearlRequestDto extends ResourceRequestDto {
  private String type;
  private double size;
  private String quality;
  private String color;
  private String shape;
}
