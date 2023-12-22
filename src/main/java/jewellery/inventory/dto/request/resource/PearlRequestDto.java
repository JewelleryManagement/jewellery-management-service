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
public class PearlRequestDto extends ResourceRequestDto {
  private String type;
  private BigDecimal size;
  private String quality;
  private String color;
  private String shape;
}
