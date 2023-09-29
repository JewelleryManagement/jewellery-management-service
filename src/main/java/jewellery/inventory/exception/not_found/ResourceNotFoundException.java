package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ResourceNotFoundException extends NotFoundException {
    public ResourceNotFoundException(UUID id) {
        super("The resource with id " + id + " is not found");
    }

    public ResourceNotFoundException(String name) {
        super("The resource with name " + name + " is not found");
    }
}
