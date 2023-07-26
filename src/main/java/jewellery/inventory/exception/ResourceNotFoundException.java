package jewellery.inventory.exception;

import java.util.UUID;

public class ResourceNotFoundException extends NotFoundException {
    public ResourceNotFoundException(UUID id) {
        super("The resource with id " + id + " is not found");
    }
}
