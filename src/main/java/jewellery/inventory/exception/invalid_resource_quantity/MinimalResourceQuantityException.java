package jewellery.inventory.exception.invalid_resource_quantity;

public class MinimalResourceQuantityException extends InvalidResourceQuantityException{
    public MinimalResourceQuantityException() {
        super("Resource quantity should not be less than 0.01");
    }
}
