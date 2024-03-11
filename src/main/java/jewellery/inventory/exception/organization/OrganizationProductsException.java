package jewellery.inventory.exception.organization;

import java.util.UUID;

public class OrganizationProductsException  extends  RuntimeException{
    public OrganizationProductsException(UUID organizationId) {
        super("Organization with id " + organizationId + " has products and cannot be deleted.");
    }
}
