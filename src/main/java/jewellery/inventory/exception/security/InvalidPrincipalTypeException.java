package jewellery.inventory.exception.security;

public class InvalidPrincipalTypeException extends RuntimeException {
  public InvalidPrincipalTypeException() {
    super("Invalid principal type.");
  }
}
