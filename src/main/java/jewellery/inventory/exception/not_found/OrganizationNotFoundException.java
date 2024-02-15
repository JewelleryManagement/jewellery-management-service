package jewellery.inventory.exception.not_found;

import java.util.UUID;

public class OrganizationNotFoundException extends NotFoundException {
    public OrganizationNotFoundException(UUID id) {
        super("The organization with id " + id + " is not found");
    }
}
