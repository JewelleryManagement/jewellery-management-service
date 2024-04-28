package jewellery.inventory.exception.sale;

public class EmptySaleException extends RuntimeException {
    public EmptySaleException() {
        super("The sale must contain either only products or only resources or both products and resources.");
    }
}
