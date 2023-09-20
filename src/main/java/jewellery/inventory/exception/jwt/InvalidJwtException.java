package jewellery.inventory.exception.jwt;


public class InvalidJwtException extends JwtAuthenticationBaseException {
  public InvalidJwtException() {
    super("Invalid JWT token");
  }
}
