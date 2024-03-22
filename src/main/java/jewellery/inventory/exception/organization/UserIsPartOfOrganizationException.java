package jewellery.inventory.exception.organization;

import java.util.UUID;

public class UserIsPartOfOrganizationException extends RuntimeException {
  public UserIsPartOfOrganizationException(UUID userId, UUID organizationId) {
    super(
        "The user with id "
            + userId
            + " is already part of the organization with id "
            + organizationId);
  }
}
