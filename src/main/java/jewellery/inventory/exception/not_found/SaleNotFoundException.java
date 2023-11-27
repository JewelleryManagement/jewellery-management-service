package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class SaleNotFoundException extends NotFoundException{

    public SaleNotFoundException(UUID saleId) {
        super("The sale with id " + saleId + " is not found");
    }
}