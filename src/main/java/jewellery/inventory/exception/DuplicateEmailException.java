package jewellery.inventory.exception;


public class DuplicateEmailException extends RuntimeException {
  public DuplicateEmailException() {
    super("User with that email already exists");
  }
}
