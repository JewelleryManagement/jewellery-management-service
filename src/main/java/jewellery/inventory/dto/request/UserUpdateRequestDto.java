package jewellery.inventory.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import jewellery.inventory.model.Role;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class UserUpdateRequestDto {
  private static final String NAME_PATTERN_VALIDATION_MSG =
      "Name must only contain alphanumeric characters and underscores, and no consecutive underscores";

  private static final String NAME_REGEX = "^(?!.*__)[\\p{L}\\d_]*$";

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

  @Enumerated(EnumType.STRING)
  private Role role;

  private String address;

  private String phone;

  private String phone2;

  @DateTimeFormat(pattern = "dd/MM/yyyy")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  @PastOrPresent(message = "Birth date must be a past or present date")
  private LocalDate birthDate;

  private String note;
}
