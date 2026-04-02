package jewellery.inventory.exception.role;

import java.util.UUID;

public class RoleAlreadyAssignedException extends RuntimeException {
  public RoleAlreadyAssignedException(UUID roleId, UUID userId, UUID organizationId) {
    super(
        "Role with id:"
            + roleId
            + " is already assigned to user with id:"
            + userId
            + " for organization with id:"
            + organizationId);
  }

  public RoleAlreadyAssignedException() {
    super("Role is assigned to members and cannot be deleted");
  }
}
