package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {
  private static final String NAME_PATTERN_VALIDATION_MSG =
      "Name must only contain alphanumeric characters and underscores, and no consecutive underscores";
  private static final String PWD_PATTERN_VALIDATION_MSG =
      "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and be at least 8 characters long";

  @NotBlank(message = "Name must not be blank, empty or null")
  @Size(min = 3, max = 64, message = "Name size must be between 3 and 64")
  @Pattern(regexp = "^(?!.*__)[A-Za-z0-9_]*$", message = NAME_PATTERN_VALIDATION_MSG)
  private String name;

  @NotBlank(message = "Email must not be blank, empty or null")
  @Pattern(
      regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
      message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password must not be blank, empty or null")
  @Size(min = 8, message = "Size must be at least 8 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
      message = PWD_PATTERN_VALIDATION_MSG)
  private String password;
}
