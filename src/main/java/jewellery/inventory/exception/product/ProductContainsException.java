package jewellery.inventory.exception.product;

import java.util.UUID;

public class ProductContainsException extends RuntimeException {
    public ProductContainsException(UUID id) {
        super("The product with id " + id + " contains in another product!");
    }
}
