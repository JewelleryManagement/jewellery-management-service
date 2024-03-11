package jewellery.inventory.exception.organization;
import java.util.UUID;

public class OrganizationResourcesException extends RuntimeException {
  public OrganizationResourcesException(UUID organizationId) {
    super("Organization with id " + organizationId + " has resources and cannot be deleted.");
  }
}
