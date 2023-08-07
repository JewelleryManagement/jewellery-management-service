package jewellery.inventory.exception.duplicate;

public class DuplicateNameException extends DuplicateException {
  public DuplicateNameException(String name) {
    super("User with name " + name + " already exists");
  }
}
