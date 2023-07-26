package jewellery.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
  private static final String NAME_PATTERN_REGEX = "^(?!.*__)[A-Za-z0-9_]*$";
  private static final String EMAIL_PATTERN_REGEX =
      "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
  private static final String NAME_SIZE_VALIDATION_MSG = "Size must be between 3 and 64";
  private static final String NAME_PATTERN_VALIDATION_MSG =
      "Name must only contain alphanumeric characters and underscores, and no consecutive underscores";
  private static final String EMAIL_VALIDATION_MSG = "Email must be valid";

  @NotEmpty
  @Size(min = 3, max = 64, message = NAME_SIZE_VALIDATION_MSG)
  @Pattern(regexp = NAME_PATTERN_REGEX, message = NAME_PATTERN_VALIDATION_MSG)
  private String name;

  @NotEmpty
  @Pattern(regexp = EMAIL_PATTERN_REGEX, message = EMAIL_VALIDATION_MSG)
  private String email;
}
