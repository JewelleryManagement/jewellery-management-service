package jewellery.inventory.exception.invalid_resource_quantity;

public class NegativeResourceQuantityException extends InvalidResourceQuantityException {
  public NegativeResourceQuantityException(double quantity) {
    super("Resource Quantity you entered is negative: -" + quantity);
  }
}
