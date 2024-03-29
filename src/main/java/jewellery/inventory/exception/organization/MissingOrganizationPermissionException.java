package jewellery.inventory.exception.organization;

import jewellery.inventory.model.OrganizationPermission;

import java.util.UUID;

public class MissingOrganizationPermissionException extends RuntimeException {
  public MissingOrganizationPermissionException(
      UUID userId, UUID organizationId, OrganizationPermission permission) {
    super(
        "User with id "
            + userId
            + " does not have "
            + permission.toString()
            + " permission for Organization with id "
            + organizationId);
  }
}
