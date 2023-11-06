package jewellery.inventory.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {

    private static final Integer MB = 1024 * 1024;

    private long maxSizeInMB;

    @Override
    public void initialize(FileSize fileSize) {
        this.maxSizeInMB = fileSize.maxSizeInMB();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {

        if (multipartFile == null) {
            return true;
        }
        return multipartFile.getSize() < maxSizeInMB * MB;
    }
}
