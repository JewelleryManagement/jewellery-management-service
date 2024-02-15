package jewellery.inventory.exception.sale;

public class SaleImpossibleException extends RuntimeException {
    public SaleImpossibleException() {
        super("The sale must contain either only products or only resources or both products and resources.");
    }
}
