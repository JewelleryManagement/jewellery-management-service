package jewellery.inventory.exception.jwt;

public class NoJwtTokenException extends JwtAuthenticationBaseException {
  public NoJwtTokenException() {
    super("No JWT token found in request.");
  }
}
