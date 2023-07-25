package jewellery.inventory.exception;

public class DuplicateEmailException extends DuplicateException {
  public DuplicateEmailException(String email) {
    super("User with email " + email + " already exists");
  }
}
