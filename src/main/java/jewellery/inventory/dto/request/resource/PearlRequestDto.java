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
public class PearlRequestDto extends ResourceRequestDto {
  @NotBlank private String type;
  @NotBlank private String size;
  @NotBlank private String quality;
  @NotBlank private String color;
  @NotBlank private String shape;
  private String shapeSpecification;
  private String colorHue;
}
