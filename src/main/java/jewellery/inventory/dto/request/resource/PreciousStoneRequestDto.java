package jewellery.inventory.dto.request.resource;

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
public class PreciousStoneRequestDto extends ResourceRequestDto {
  private String color;
  @Positive private BigDecimal carat;
  private String cut;
  private String clarity;
  @Positive private BigDecimal dimensionX;
  @Positive private BigDecimal dimensionY;
  @Positive private BigDecimal dimensionZ;
  private String shape;
  private String type;
  private String colorHue;
  private String polish;
  private String symmetry;
  private String fluorescence;
  private String certificate;
}
