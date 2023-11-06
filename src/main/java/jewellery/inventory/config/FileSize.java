package jewellery.inventory.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {

    Class<? extends Payload>[] payload() default {};
    Class<?>[] groups() default {};
    long maxSizeInMB() default 2;
    String message() default "File too large!";
}
