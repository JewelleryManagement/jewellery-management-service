package jewellery.inventory.helper;

import java.util.EnumSet;
import java.util.UUID;
import jewellery.inventory.dto.request.ScopedRoleRequestDto;
import jewellery.inventory.model.Permission;
import jewellery.inventory.model.ScopedRole;

public class ScopedRoleHelper {
  public static ScopedRoleRequestDto createRoleRequest() {
    return new ScopedRoleRequestDto("ORGANIZATION_ADMIN", EnumSet.allOf(Permission.class));
  }

  public static ScopedRole createRole(ScopedRoleRequestDto scopedRoleRequestDto) {
    return new ScopedRole(
        UUID.randomUUID(), scopedRoleRequestDto.getName(), scopedRoleRequestDto.getPermissions());
  }
}
