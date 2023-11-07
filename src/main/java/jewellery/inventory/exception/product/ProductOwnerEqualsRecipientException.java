package jewellery.inventory.exception.product;

import java.util.UUID;

public class ProductOwnerEqualsRecipientException extends RuntimeException {
  public ProductOwnerEqualsRecipientException(UUID ownerId) {
    super("Owner and Recipient cannot be the same. ID :" + ownerId);
  }
}
