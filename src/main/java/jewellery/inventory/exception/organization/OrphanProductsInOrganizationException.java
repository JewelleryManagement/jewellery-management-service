package jewellery.inventory.exception.organization;

import java.util.UUID;

public class OrphanProductsInOrganizationException extends  RuntimeException{
    public OrphanProductsInOrganizationException(UUID organizationId) {
        super("Organization with id " + organizationId + " has products and cannot be deleted.");
    }
}
