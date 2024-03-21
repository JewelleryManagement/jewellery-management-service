package jewellery.inventory.exception.organization;
import java.util.UUID;

public class OrphanResourcesInOrganizationException extends RuntimeException {
  public OrphanResourcesInOrganizationException(UUID organizationId) {
    super("Organization with id " + organizationId + " has resources and cannot be deleted.");
  }
}
