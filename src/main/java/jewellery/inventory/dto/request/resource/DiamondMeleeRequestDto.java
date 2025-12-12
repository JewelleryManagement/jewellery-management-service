package jewellery.inventory.dto.request.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class DiamondMeleeRequestDto extends ResourceRequestDto {
  @NotBlank private String color;
  @NotBlank private String cut;
  @NotBlank private String clarity;
  @NotBlank private String shape;
  @NotBlank private String size;
  @NotBlank private String type;
  @NotNull @Positive private BigDecimal carat;
}
