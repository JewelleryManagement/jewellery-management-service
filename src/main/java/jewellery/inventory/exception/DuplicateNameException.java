package jewellery.inventory.exception;

public class DuplicateNameException extends RuntimeException {
  public DuplicateNameException() {
    super("User with that name already exists");
  }
}
