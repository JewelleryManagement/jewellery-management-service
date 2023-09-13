package jewellery.inventory.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDto {
  private String name;

  private String email;

  private String password;
}
