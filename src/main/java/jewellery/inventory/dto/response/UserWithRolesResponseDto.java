package jewellery.inventory.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UserWithRolesResponseDto {
  private UserResponseDto user;
  private List<ScopedRoleResponseDto> roles = new ArrayList<>();
}
