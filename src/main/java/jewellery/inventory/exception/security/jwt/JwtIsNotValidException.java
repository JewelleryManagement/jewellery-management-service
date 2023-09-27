package jewellery.inventory.exception.security.jwt;

import io.jsonwebtoken.security.SignatureException;

public class JwtIsNotValidException extends SignatureException {
  public JwtIsNotValidException() {
    super("Invalid JWT token");
  }

  public JwtIsNotValidException(String message) {
    super(message);
  }
}
