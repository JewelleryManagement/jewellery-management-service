package jewellery.inventory.exception;

public class DuplicateNameException extends DuplicateException {
  public DuplicateNameException(String name) {
    super("User with name " + name + " already exists");
  }
}
