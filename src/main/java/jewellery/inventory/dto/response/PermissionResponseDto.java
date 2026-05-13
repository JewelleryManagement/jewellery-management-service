package jewellery.inventory.dto.response;

import java.util.Set;
import jewellery.inventory.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {
  private Permission permission;
  private Set<Permission> included;
}
