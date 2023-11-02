package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ImageNotFoundException extends NotFoundException{
    public ImageNotFoundException(String name) {
        super("Image with id " + name + " is not found!");
    }
}
