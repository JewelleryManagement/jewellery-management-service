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
public class PearlResponseDto extends ResourceResponseDto {
  private String type;
  private BigDecimal size;
  private String quality;
  private String color;
  private String shape;
}
