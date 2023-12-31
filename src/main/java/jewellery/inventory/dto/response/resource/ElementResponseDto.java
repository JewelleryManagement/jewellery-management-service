package jewellery.inventory.dto.response.resource;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ElementResponseDto extends ResourceResponseDto {
  private String description;
}
