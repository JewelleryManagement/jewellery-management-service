package jewellery.inventory.exception.security.jwt;

public class JwtIsNotValidException extends JwtAuthenticationBaseException {
  public JwtIsNotValidException() {
    super("Invalid JWT token");
  }
}
