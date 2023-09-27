package jewellery.inventory.exception.security.jwt;

public class JwtNameInIsNotValidException extends JwtIsNotValidException {
    public JwtNameInIsNotValidException() {
        super("Invalid email in token");
    }
}
