package jewellery.inventory.exception;

import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jewellery.inventory.exception.duplicate.DuplicateException;
import jewellery.inventory.exception.invalid_resource_quantity.InvalidResourceQuantityException;
import jewellery.inventory.exception.not_found.NotFoundException;
import jewellery.inventory.exception.not_found.ResourceInUserNotFoundException;
import jewellery.inventory.exception.product.*;
import jewellery.inventory.exception.security.InvalidSecretKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static final String DATE_TIME_FORMAT_PATTERN = "HH:mm 'on' dd/MM/yyyy";
  private static final String TIMESTAMP_KEY = "timestamp";
  private static final String ERROR_KEY = "error";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, List<String>> fieldErrorsMap = getFieldErrors(ex);
    return createErrorResponse(HttpStatus.BAD_REQUEST, fieldErrorsMap);
  }

  @ExceptionHandler({NotFoundException.class, ResourceInUserNotFoundException.class})
  public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
    return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String error = String.format("%s should be of type %s", ex.getName(), getRequiredTypeName(ex));
    return createErrorResponse(HttpStatus.BAD_REQUEST, error);
  }

  @ExceptionHandler({InvalidResourceQuantityException.class, DuplicateException.class})
  public ResponseEntity<Object> handleBadDataExceptions(RuntimeException ex) {
    return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler({
    ProductIsContentException.class,
    ProductIsSoldException.class,
    SellerNotOwnerException.class,
    ProductOwnerEqualsRecipientException.class,
    ProductOwnerNotSeller.class
  })
  public ResponseEntity<Object> handleEntityConstraintConflict(RuntimeException ex) {
    return createErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler({SignatureException.class, AuthenticationException.class})
  public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler({InvalidSecretKeyException.class})
  public ResponseEntity<Object> handleBadSecretKey(RuntimeException ex) {
    return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  private ResponseEntity<Object> createErrorResponse(HttpStatus status, Object error) {
    Map<String, Object> body = new HashMap<>();
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN));
    body.put(TIMESTAMP_KEY, date);
    body.put(ERROR_KEY, error);

    return new ResponseEntity<>(body, status);
  }

  private static Map<String, List<String>> getFieldErrors(MethodArgumentNotValidException ex) {
    List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

    Map<String, List<String>> fieldErrorsMap = new HashMap<>();
    for (FieldError error : fieldErrors) {
      String fieldName = error.getField();
      String errorMessage =
          error.getDefaultMessage() != null
              ? error.getDefaultMessage()
              : "Error message not available";

      if (!fieldErrorsMap.containsKey(fieldName)) {
        fieldErrorsMap.put(fieldName, new ArrayList<>());
      }

      fieldErrorsMap.get(fieldName).add(errorMessage);
    }
    return fieldErrorsMap;
  }

  private static String getRequiredTypeName(MethodArgumentTypeMismatchException ex) {
    Class<?> requiredType = ex.getRequiredType();
    if (requiredType == null) {
      throw new IllegalStateException("Required type for " + ex.getName() + " was null");
    }
    return requiredType.getName();
  }
}
