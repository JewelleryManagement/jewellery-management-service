package jewellery.inventory.exception;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
  public UserNotFoundException(UUID id) {
    super("User with id " + id + " was not found");
  }
}
