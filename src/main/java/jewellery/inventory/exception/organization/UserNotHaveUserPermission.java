package jewellery.inventory.exception.organization;

import java.util.UUID;

public class UserNotHaveUserPermission extends RuntimeException {
  public UserNotHaveUserPermission(UUID userId, UUID organizationId) {
    super(
        "The User with id "
            + userId
            + " not have MANAGE_USERS permission for Organization with id "
            + organizationId);
  }
}
