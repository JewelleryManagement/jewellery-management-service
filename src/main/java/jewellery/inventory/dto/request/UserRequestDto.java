package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserRequestDto extends UserUpdateRequestDto {
  private static final String PWD_PATTERN_VALIDATION_MSG =
      "Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long";

  @NotBlank(message = "Password must not be blank, empty or null")
  @Size(min = 8, message = "Password must be at least 8 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
      message = PWD_PATTERN_VALIDATION_MSG)
  private String password;
}
