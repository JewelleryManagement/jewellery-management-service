package jewellery.inventory.dto.response;

import java.util.UUID;
import lombok.Data;

@Data
public class UserResponseDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
}
