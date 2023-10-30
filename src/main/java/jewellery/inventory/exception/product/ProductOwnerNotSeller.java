package jewellery.inventory.exception.product;

import java.util.UUID;

public class ProductOwnerNotSeller extends RuntimeException {
  public ProductOwnerNotSeller(UUID ownerId, UUID sellerId) {
    super("The seller with ID "
            + sellerId
            + " cannot sell a product that is not owned by them. Owner ID: "+ownerId);
  }
}
