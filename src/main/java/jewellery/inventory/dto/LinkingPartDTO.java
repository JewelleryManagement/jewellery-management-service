package jewellery.inventory.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class LinkingPartDTO extends ResourceDTO{
  private String description;
}
