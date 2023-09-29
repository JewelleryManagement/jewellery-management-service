package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestDto {
  @NotBlank(message = "Email must not be blank, empty or null")
  private String email;

  @NotBlank(message = "Password must not be blank, empty or null")
  private String password;
}
