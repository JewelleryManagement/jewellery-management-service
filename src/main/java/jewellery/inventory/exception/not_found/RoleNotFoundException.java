package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class RoleNotFoundException extends NotFoundException {
  public RoleNotFoundException(String name) {
    super("Role with name: " + name + " does not exists!");
  }

  public RoleNotFoundException(UUID id) {
    super("Role with id: " + id + " does not exists!");
  }
}
