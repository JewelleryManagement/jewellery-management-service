package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class ResourceNotFoundInSaleException extends NotFoundException{
    public ResourceNotFoundInSaleException(UUID resourceId, UUID saleId) {
        super("Resource with id " + resourceId + " was not found in sale with id " + saleId);
    }
}
