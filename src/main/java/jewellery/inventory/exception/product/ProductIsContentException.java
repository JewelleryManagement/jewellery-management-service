package jewellery.inventory.exception.product;

import java.util.UUID;

public class ProductIsContentException extends RuntimeException {
    public ProductIsContentException(UUID id) {
        super("The product with id " + id + " is content of another product!");
    }
}
