package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
  public UserNotFoundException(UUID id) {
    super("User with id " + id + " was not found");
  }

  public UserNotFoundException(String name) {
    super("User with name " + name + " was not found");
  }
}
