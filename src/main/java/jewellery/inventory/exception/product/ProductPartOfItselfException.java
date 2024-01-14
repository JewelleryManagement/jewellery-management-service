package jewellery.inventory.exception.product;

public class ProductPartOfItselfException extends RuntimeException {
  public ProductPartOfItselfException() {
    super("The edited product cannot be part of its content");
  }
}
