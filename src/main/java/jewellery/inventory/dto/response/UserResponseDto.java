package jewellery.inventory.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class UserResponseDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private String address;
  private String phone;
  private String phone2;
  private LocalDate birthDate;
  private String note;
}
