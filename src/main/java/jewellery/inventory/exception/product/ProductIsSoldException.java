package jewellery.inventory.exception.product;

import java.util.UUID;

public class ProductIsSoldException extends RuntimeException {
    public ProductIsSoldException(UUID id) {
        super("The product with id " + id + " is sold!");
    }
}
