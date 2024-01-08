package jewellery.inventory.dto.response;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserQuantityResponseDto {
  private UserResponseDto owner;
  private BigDecimal quantity;
}
