package jewellery.inventory.helper;

import java.util.EnumSet;
import java.util.UUID;
import jewellery.inventory.dto.request.RoleRequestDto;
import jewellery.inventory.model.OrganizationRole;
import jewellery.inventory.model.Permission;

public class RoleHelper {
  public static RoleRequestDto createRoleRequest() {
    return new RoleRequestDto("ORGANIZATION_ADMIN", EnumSet.allOf(Permission.class));
  }

  public static OrganizationRole createRole(RoleRequestDto roleRequestDto) {
    return new OrganizationRole(
        UUID.randomUUID(), roleRequestDto.getName(), roleRequestDto.getPermissions());
  }
}
