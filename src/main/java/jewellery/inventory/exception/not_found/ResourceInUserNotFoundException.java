package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ResourceInUserNotFoundException extends NotFoundException {
  public ResourceInUserNotFoundException(UUID resourceId, UUID userId) {
    super("The resource with id " + resourceId + " is not owned by user with id " + userId);
  }
}
