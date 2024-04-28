package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ResourceInOrganizationNotFoundException extends NotFoundException {
  public ResourceInOrganizationNotFoundException(UUID resourceId, UUID organizationId) {
    super(
        "The resource with id "
            + resourceId
            + " is not owned by organization with id "
            + organizationId);
  }

  public ResourceInOrganizationNotFoundException(UUID organizationId) {
    super("Organization with id " + organizationId + " has no resources!");
  }
}
