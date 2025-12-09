package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ElementRequestDto extends ResourceRequestDto {
  @NotBlank private String description;
}
