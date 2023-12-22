package jewellery.inventory.dto;

import java.math.BigDecimal;
import jewellery.inventory.dto.response.UserResponseDto;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserQuantityDto {
  private UserResponseDto owner;
  private BigDecimal quantity;
}
