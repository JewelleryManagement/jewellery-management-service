package jewellery.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;
import jewellery.inventory.model.Role;
import lombok.Data;

@Data
public class DetailedUserResponseDto {
  private UUID id;
  private String firstName;
  private String lastName;
  private String email;
  private Role role;
  private String address;
  private String phone;
  private String phone2;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private LocalDate birthDate;
  private String note;
}
