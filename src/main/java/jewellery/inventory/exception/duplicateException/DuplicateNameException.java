package jewellery.inventory.exception.duplicateException;

public class DuplicateNameException extends DuplicateException {
  public DuplicateNameException(String name) {
    super("User with name " + name + " already exists");
  }
}
