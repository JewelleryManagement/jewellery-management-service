package jewellery.inventory.exception.jwt;

public class JwtExpiredException extends JwtAuthenticationBaseException  {

  private static final String CUSTOM_MESSAGE = "JWT token has expired.";

  public JwtExpiredException() {
    super(CUSTOM_MESSAGE);
  }
}
