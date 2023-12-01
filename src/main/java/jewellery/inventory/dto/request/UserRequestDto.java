package jewellery.inventory.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class UserRequestDto {
  private static final String NAME_PATTERN_VALIDATION_MSG =
      "Name must only contain alphanumeric characters and underscores, and no consecutive underscores";
  private static final String PWD_PATTERN_VALIDATION_MSG =
      "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and be at least 8 characters long";

  private static final String NAME_REGEX = "^(?!.*__)[\\w]*$";

  @NotBlank(message = "First name must not be blank, empty or null")
  @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
  @Pattern(regexp = NAME_REGEX, message = NAME_PATTERN_VALIDATION_MSG)
  private String firstName;

  @NotBlank(message = "Last name must not be blank, empty or null")
  @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
  @Pattern(regexp = NAME_REGEX, message = NAME_PATTERN_VALIDATION_MSG)
  private String lastName;

  @NotBlank(message = "Email must not be blank, empty or null")
  @Pattern(
      regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
      message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password must not be blank, empty or null")
  @Size(min = 8, message = "Password must be at least 8 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
      message = PWD_PATTERN_VALIDATION_MSG)
  private String password;

  @Size(min = 3, max = 100, message = "Address must be between 3 and 100 characters")
  private String address;

  private String phone;

  private String phone2;

  @DateTimeFormat(pattern = "dd/MM/yyyy")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  @PastOrPresent(message = "Birth date must be a past or present date")
  private LocalDate birthDate;

  @Size(min = 3, max = 500, message = "Note must be between 3 and 500 characters")
  private String note;
}
