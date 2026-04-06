package jewellery.inventory.dto.request;

import java.util.Set;
import jewellery.inventory.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScopedRoleRequestDto {
  private String name;
  private Set<Permission> permissions;
}
