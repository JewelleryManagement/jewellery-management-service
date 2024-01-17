package jewellery.inventory.exception.resources;

import java.util.UUID;

public class ResourceIsPartOfProductException extends RuntimeException {
  public ResourceIsPartOfProductException(UUID resourceId, UUID productId) {
    super("Resource with ID " + resourceId + " is part of product with ID " + productId);
  }
}
