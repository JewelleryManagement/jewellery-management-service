package jewellery.inventory.exception.security;

public class InvalidSecretKeyException extends RuntimeException {
  public InvalidSecretKeyException() {
    super("Invalid secret key");
  }
}
