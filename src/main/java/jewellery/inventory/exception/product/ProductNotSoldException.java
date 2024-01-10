package jewellery.inventory.exception.product;

import java.util.UUID;

public class ProductNotSoldException extends RuntimeException {
  public ProductNotSoldException(UUID id) {
    super("The product with id " + id + " has not been sold!");
  }
}