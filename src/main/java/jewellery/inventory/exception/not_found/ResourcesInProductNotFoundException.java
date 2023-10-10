package jewellery.inventory.exception.not_found;

import jewellery.inventory.exception.not_found.NotFoundException;

public class ResourcesInProductNotFoundException extends NotFoundException {
    public ResourcesInProductNotFoundException() {
        super("The product cannot be created without resources");
    }
}
