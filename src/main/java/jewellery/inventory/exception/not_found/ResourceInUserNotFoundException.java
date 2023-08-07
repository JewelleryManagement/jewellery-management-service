package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ResourceInUserNotFoundException extends NotFoundException {
    public ResourceInUserNotFoundException(UUID id) {
        super("The resource with id " + id + " is not owned by this user");
    }
}
