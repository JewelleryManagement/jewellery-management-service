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
public class PreciousStoneResponseDto extends ResourceResponseDto {
  private String color;
  private BigDecimal carat;
  private String cut;
  private String clarity;
  private String size;
  private String shape;
}
