package jewellery.inventory.exception.image;

public class MultipartFileContentTypeException extends RuntimeException {
  public MultipartFileContentTypeException() {
    super("Only PNG or JPG images are allowed.");
  }

  public MultipartFileContentTypeException(String message) {
    super(message);
  }
}
