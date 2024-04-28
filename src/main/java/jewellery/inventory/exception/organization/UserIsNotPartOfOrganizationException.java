package jewellery.inventory.exception.organization;

import java.util.UUID;

public class UserIsNotPartOfOrganizationException  extends  RuntimeException{
    public UserIsNotPartOfOrganizationException(UUID userId, UUID organizationId) {
        super(
                "The User with id "
                        + userId
                        + "is not part of Organization with id "
                        + organizationId);
    }
}
