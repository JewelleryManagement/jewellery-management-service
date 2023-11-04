package jewellery.inventory.exception.not_found;

public class ImageNotFoundException extends NotFoundException{
    public ImageNotFoundException(String name) {
        super("Image with name " + name + " is not found!");
    }

    public ImageNotFoundException() {
        super("You have not selected an image!");
    }
}
