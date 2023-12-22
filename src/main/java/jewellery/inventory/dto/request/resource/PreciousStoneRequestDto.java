package jewellery.inventory.dto.request.resource;

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
  private BigDecimal carat;
  private String cut;
  private String clarity;
  private BigDecimal dimensionX;
  private BigDecimal dimensionY;
  private BigDecimal dimensionZ;
  private String shape;
}
