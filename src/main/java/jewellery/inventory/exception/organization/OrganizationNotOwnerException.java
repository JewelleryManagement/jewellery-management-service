package jewellery.inventory.exception.organization;


import java.util.UUID;

public class OrganizationNotOwnerException extends RuntimeException {
    public OrganizationNotOwnerException(UUID organizationId, UUID productId) {
        super("Organization with id " + organizationId + " is not the owner of a product with id " + productId);
    }
 }
