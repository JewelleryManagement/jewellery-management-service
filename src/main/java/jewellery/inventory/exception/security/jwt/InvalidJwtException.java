package jewellery.inventory.exception.security.jwt;

public class InvalidJwtException extends JwtAuthenticationBaseException {
  public InvalidJwtException() {
    super("Invalid JWT token");
  }
}
