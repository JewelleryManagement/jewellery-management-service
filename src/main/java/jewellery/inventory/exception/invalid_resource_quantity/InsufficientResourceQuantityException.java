package jewellery.inventory.exception.invalid_resource_quantity;

import java.math.BigDecimal;

public class InsufficientResourceQuantityException extends InvalidResourceQuantityException {
  public InsufficientResourceQuantityException(BigDecimal quantity, BigDecimal total) {
    super(
        "Insufficient Resource Quantity. Tried to remove "
            + quantity
            + " but total is: "
            + total);
  }
}
