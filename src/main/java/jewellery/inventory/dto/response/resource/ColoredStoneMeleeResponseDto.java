package jewellery.inventory.dto.response.resource;

import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ColoredStoneMeleeResponseDto extends ResourceResponseDto {
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
