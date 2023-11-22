package jewellery.inventory.exception.image;

public class MultipartFileNotSelectedException extends RuntimeException{
    public MultipartFileNotSelectedException() {
        super("Please select a file!");
    }
}
