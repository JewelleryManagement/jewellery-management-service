package jewellery.inventory.exception.security.jwt;

public class InvalidNameInJwtException extends RuntimeException  {
    public InvalidNameInJwtException() {
        super("Invalid email in token");
    }
}
