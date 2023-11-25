package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ImageNotFoundException extends NotFoundException{
    public ImageNotFoundException(UUID productId) {
        super("Product with id " + productId + " has no attached picture.");
    }
}
