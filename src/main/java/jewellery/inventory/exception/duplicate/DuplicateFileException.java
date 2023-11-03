package jewellery.inventory.exception.duplicate;

public class DuplicateFileException extends DuplicateException{
    public DuplicateFileException(String name) {
        super("File with name " + name + " already exists");
    }
}
