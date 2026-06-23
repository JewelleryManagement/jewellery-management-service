package jewellery.inventory.exception.role;

import jewellery.inventory.model.Permission;
import jewellery.inventory.model.RoleType;

public class InvalidRolePermissionException extends RuntimeException {

  public InvalidRolePermissionException(Permission permission, RoleType roleType) {
    super("Permission " + permission + " is not allowed for role type " + roleType);
  }
}
