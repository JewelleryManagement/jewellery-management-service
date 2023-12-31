package jewellery.inventory.exception.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationBaseException extends AuthenticationException {
  public JwtAuthenticationBaseException(String msg) {
    super(msg);
  }

  public JwtAuthenticationBaseException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
