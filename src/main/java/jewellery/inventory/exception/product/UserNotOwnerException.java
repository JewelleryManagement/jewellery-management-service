package jewellery.inventory.exception.product;

import java.util.UUID;

public class UserNotOwnerException extends RuntimeException {
    public UserNotOwnerException(UUID userId, UUID productId) {
        super("User with id " + userId + " is not the owner of a product with id " + productId);
    }
}
