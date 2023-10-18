package jewellery.inventory.exception.security;

import jewellery.inventory.exception.security.jwt.JwtAuthenticationBaseException;


public class InvalidCredentialsException extends JwtAuthenticationBaseException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
