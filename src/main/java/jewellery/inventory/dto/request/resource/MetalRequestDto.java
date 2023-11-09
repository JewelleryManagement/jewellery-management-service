package jewellery.inventory.dto.request.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class MetalRequestDto extends ResourceRequestDto {
  private String type;
  private int purity;
  private String color;
  private String plating;
}
