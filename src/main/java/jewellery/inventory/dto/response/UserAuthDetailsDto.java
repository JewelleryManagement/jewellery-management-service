package jewellery.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAuthDetailsDto {
    String token;
    UserResponseDto user;
}
