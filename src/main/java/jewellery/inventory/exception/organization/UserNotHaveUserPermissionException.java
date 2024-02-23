package jewellery.inventory.exception.organization;

import java.util.UUID;

public class UserNotHaveUserPermissionException extends RuntimeException {
  public UserNotHaveUserPermissionException(UUID userId, UUID organizationId) {
    super(
        "The User with id "
            + userId
            + " not have MANAGE_USERS permission for Organization with id "
            + organizationId);
  }
}
