package jewellery.inventory.exception.not_found;

public class ResourcesInProductNotFoundException extends NotFoundException {
    public ResourcesInProductNotFoundException() {
        super("The product cannot be created without resources");
    }
}
