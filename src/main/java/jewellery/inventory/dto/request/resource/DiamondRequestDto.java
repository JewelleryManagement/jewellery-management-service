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
public class DiamondRequestDto extends ResourceRequestDto {
  @NotBlank private String color;
  @NotNull @Positive private BigDecimal carat;
  @NotBlank private String cut;
  @NotBlank private String clarity;
  @NotNull @Positive private BigDecimal dimensionX;
  @NotNull @Positive private BigDecimal dimensionY;
  @NotNull @Positive private BigDecimal dimensionZ;
  @NotBlank private String shape;
  @NotBlank private String type;
  private String colorHue;
  @NotBlank private String polish;
  @NotBlank private String symmetry;
  @NotBlank private String fluorescence;
  private String certificate;
}
