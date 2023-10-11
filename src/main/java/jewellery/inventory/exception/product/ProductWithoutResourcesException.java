package jewellery.inventory.exception.product;

public class ProductWithoutResourcesException extends RuntimeException {
    public ProductWithoutResourcesException() {
        super("The product cannot be created without resources");
    }
}
