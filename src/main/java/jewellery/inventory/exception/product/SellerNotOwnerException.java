package jewellery.inventory.exception.product;

import java.util.UUID;

public class SellerNotOwnerException extends RuntimeException {
    public SellerNotOwnerException(UUID userId, UUID productId) {
        super("User with id " + userId + " is not the owner of a product with id " + productId);
    }
}
