package jewellery.inventory.exception.security.jwt;

public class JwtTokenNotFoundException extends JwtAuthenticationBaseException {
  public JwtTokenNotFoundException() {
    super("No JWT token found in request.");
  }
}
