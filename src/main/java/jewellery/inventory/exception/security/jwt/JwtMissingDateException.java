package jewellery.inventory.exception.security.jwt;

public class JwtMissingDateException extends JwtIsNotValidException {
    public JwtMissingDateException() {
        super("Expiration date is missing in the token");
    }
}
