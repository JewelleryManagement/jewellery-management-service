package jewellery.inventory.exception.security.jwt;

import io.jsonwebtoken.security.SignatureException;

public class JwtIsNotValidException extends JwtAuthenticationBaseException {
  public JwtIsNotValidException() {
    super("Invalid JWT token");
  }

  public JwtIsNotValidException(SignatureException e) {
    super("Invalid JWT token: " + e.getMessage(), e.getCause());
  }
}
