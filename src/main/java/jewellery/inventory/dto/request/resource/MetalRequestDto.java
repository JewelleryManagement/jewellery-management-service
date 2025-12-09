package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class MetalRequestDto extends ResourceRequestDto {
  @NotBlank private String type;
  @NotNull @Positive private int purity;
  @NotBlank private String color;
}
