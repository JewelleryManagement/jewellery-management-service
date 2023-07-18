package jewellery.inventory.dto;

import static jewellery.inventory.constant.ApplicationConstants.EMAIL_PATTERN_REGEX;
import static jewellery.inventory.constant.ApplicationConstants.EMAIL_VALIDATION_MSG;
import static jewellery.inventory.constant.ApplicationConstants.NAME_PATTERN_REGEX;
import static jewellery.inventory.constant.ApplicationConstants.NAME_PATTERN_VALIDATION_MSG;
import static jewellery.inventory.constant.ApplicationConstants.NAME_SIZE_VALIDATION_MSG;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
  @NotEmpty
  @Size(min = 3, max = 64, message = NAME_SIZE_VALIDATION_MSG)
  @Pattern(regexp = NAME_PATTERN_REGEX, message = NAME_PATTERN_VALIDATION_MSG)
  private String name;

  @NotEmpty
  @Pattern(regexp = EMAIL_PATTERN_REGEX, message = EMAIL_VALIDATION_MSG)
  private String email;
}
