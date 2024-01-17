package jewellery.inventory.exception.resources;

import java.util.UUID;

public class ResourceSoldException extends RuntimeException {
    public ResourceSoldException(UUID id) {
        super("The resource with id " + id + " is sold!");
    }
}
