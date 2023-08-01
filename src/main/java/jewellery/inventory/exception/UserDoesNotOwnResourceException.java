package jewellery.inventory.exception;

import java.util.UUID;
import jewellery.inventory.exception.notFoundException.NotFoundException;

public class UserDoesNotOwnResourceException extends NotFoundException {
  public UserDoesNotOwnResourceException(UUID userId, UUID resourceId) {
    super("User with ID " + userId + " does not own the resource with ID " + resourceId);
  }
}
