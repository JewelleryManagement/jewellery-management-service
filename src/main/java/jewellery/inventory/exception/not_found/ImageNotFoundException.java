package jewellery.inventory.exception.not_found;

public class ImageNotFoundException extends NotFoundException{
    public ImageNotFoundException(String name) {
        super("Image with name " + name + " is not found!");
    }
}
