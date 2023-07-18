package jewellery.inventory.constant;

public class ApplicationConstants {
    //Error messages
    public static final String USER_NOT_FOUND_ERROR_MSG = "User not found with id: ";
    public static final String DUPLICATE_EMAIL_ERROR_MSG = "Email already exists";
    public static final String DUPLICATE_NAME_ERROR_MSG = "Name already exists";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String ERROR_KEY = "error";

    //Validation messages
    public static final String NAME_SIZE_VALIDATION_MSG = "Size must be between 3 and 64";
    public static final String NAME_PATTERN_VALIDATION_MSG = "Name must only contain alphanumeric characters and underscores, and no consecutive underscores";
    public static final String EMAIL_VALIDATION_MSG = "Email must be valid";

    //Regex patterns
    public static final String NAME_PATTERN_REGEX = "^(?!.*__)[A-Za-z0-9_]*$";
    public static final String EMAIL_PATTERN_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    //Datetime format patterns
    public static final String DATE_TIME_FORMAT_PATTERN = "HH:mm 'on' dd/MM/yyyy";

}
