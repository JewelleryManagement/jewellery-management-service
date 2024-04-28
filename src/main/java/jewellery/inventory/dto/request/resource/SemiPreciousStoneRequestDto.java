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
public class SemiPreciousStoneRequestDto extends ResourceRequestDto {
  private String color;
  private String cut;
  private String clarity;
  private String shape;
  @Positive private BigDecimal size;
}
