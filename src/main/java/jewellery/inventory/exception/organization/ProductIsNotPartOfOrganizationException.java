package jewellery.inventory.exception.organization;

import java.util.UUID;

public class ProductIsNotPartOfOrganizationException extends RuntimeException {
  public ProductIsNotPartOfOrganizationException(UUID productId) {
    super("The Product with id " + productId + "is not owned of Organization");
  }
}
