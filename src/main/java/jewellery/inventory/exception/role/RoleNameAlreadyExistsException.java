package jewellery.inventory.exception.role;


public class RoleNameAlreadyExistsException extends RuntimeException {
  public RoleNameAlreadyExistsException(String name) {
    super("Role with name: " + name + " already exists!");
  }
}
