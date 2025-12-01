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
public class ColoredStoneMeleeRequestDto extends ResourceRequestDto {
  private String color;
  private String cut;
  private String clarity;
  private String shape;
  private String size;
  private String type;
  private BigDecimal carat;
  private String colorHue;
  private String treatment;
}
