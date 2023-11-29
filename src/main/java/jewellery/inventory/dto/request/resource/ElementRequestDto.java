package jewellery.inventory.dto.request.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ElementRequestDto extends ResourceRequestDto {
  private String description;
}
