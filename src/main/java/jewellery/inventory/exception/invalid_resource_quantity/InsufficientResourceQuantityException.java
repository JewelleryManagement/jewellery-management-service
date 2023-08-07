package jewellery.inventory.exception.invalid_resource_quantity;

public class InsufficientResourceQuantityException extends InvalidResourceQuantityException {
  public InsufficientResourceQuantityException(double quantity, double total) {
    super(
        "Insufficient Resource Quantity. Tried to remove "
            + quantity
            + " but total is: "
            + total);
  }
}
