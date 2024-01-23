package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ResourceNotSoldException extends NotFoundException{
    public ResourceNotSoldException(UUID resourceId, UUID saleId) {
        super("Resource with id " + resourceId + " was not found in sale with id " + saleId);
    }
}
