package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {
  private static final String NAME_PATTERN_REGEX = "^(?!.*__)[A-Za-z0-9_]*$";
  private static final String EMAIL_PATTERN_REGEX =
      "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
  private static final String PWD_PATTERN_REGEX =
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
  private static final String NAME_SIZE_VALIDATION_MSG = "Size must be between 3 and 64";
  private static final String PWD_SIZE_VALIDATION_MSG = "Size must be at least 8 characters";
  private static final String NAME_PATTERN_VALIDATION_MSG =
      "Name must only contain alphanumeric characters and underscores, and no consecutive underscores";
  private static final String EMAIL_VALIDATION_MSG = "Email must be valid";
  private static final String PWD_PATTERN_VALIDATION_MSG =
      "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and be at least 8 characters long";

  @NotBlank(message = "Name must not be blank, empty or null")
  @Size(min = 3, max = 64, message = NAME_SIZE_VALIDATION_MSG)
  @Pattern(regexp = NAME_PATTERN_REGEX, message = NAME_PATTERN_VALIDATION_MSG)
  private String name;

  @NotBlank(message = "Email must not be blank, empty or null")
  @Pattern(regexp = EMAIL_PATTERN_REGEX, message = EMAIL_VALIDATION_MSG)
  private String email;

  @NotBlank(message = "Password must not be blank, empty or null")
  @Size(min = 8, message = PWD_SIZE_VALIDATION_MSG)
  @Pattern(regexp = PWD_PATTERN_REGEX, message = PWD_PATTERN_VALIDATION_MSG)
  private String password;
}
