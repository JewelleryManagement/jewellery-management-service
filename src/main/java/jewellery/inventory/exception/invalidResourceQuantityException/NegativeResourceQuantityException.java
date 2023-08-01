package jewellery.inventory.exception.invalidResourceQuantityException;

public class NegativeResourceQuantityException extends InvalidResourceQuantityException {
  public NegativeResourceQuantityException(double quantity) {
    super("Resource Quantity you entered is negative: " + quantity);
  }
}
