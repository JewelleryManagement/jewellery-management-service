package jewellery.inventory.exception.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationBaseException extends AuthenticationException {
    public JwtAuthenticationBaseException(String msg) {
    super(msg);
  }
}
