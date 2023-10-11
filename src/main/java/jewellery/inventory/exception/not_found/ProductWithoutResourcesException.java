package jewellery.inventory.exception.not_found;

public class ProductWithoutResourcesException extends NotFoundException {
    public ProductWithoutResourcesException() {
        super("The product cannot be created without resources");
    }
}
