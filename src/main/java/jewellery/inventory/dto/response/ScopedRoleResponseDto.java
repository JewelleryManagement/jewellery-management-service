package jewellery.inventory.dto.response;

import java.util.Set;
import java.util.UUID;
import jewellery.inventory.model.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScopedRoleResponseDto {
  private UUID id;
  private String name;
  private RoleType roleType;
  private Set<PermissionResponseDto> permissions;
}
