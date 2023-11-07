package jewellery.inventory.dto;

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
  private double quantity;
}
