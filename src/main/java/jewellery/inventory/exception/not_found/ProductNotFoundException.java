package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ProductNotFoundException extends NotFoundException{

    public ProductNotFoundException(UUID id) {
        super("The product with id " + id + " is not found");
    }
    public ProductNotFoundException(String name) {
        super("The product with name " + name + " is not found");
    }
}
