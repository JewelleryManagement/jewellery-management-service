package jewellery.inventory.exception.not_found;

public class NoAuthenticatedUserException extends NotFoundException {
  public NoAuthenticatedUserException() {
    super("No authenticated user found.");
  }
}
