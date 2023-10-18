package jewellery.inventory.exception.security;

import io.jsonwebtoken.security.WeakKeyException;

public class InvalidSecretKeyException extends RuntimeException {
  public InvalidSecretKeyException(WeakKeyException e) {
    super("Invalid secret key: " + e.getMessage(), e.getCause());
  }
}
