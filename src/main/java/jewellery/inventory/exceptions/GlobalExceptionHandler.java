package jewellery.inventory.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
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

    Map<String, Object> body = new HashMap<>();
    body.put(
        "timestamp",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM/yyyy")));
    body.put("errors", fieldErrorsMap);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Object> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {

    Map<String, Object> body = new HashMap<>();
    body.put(
        "timestamp",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM/yyyy")));
    body.put("error", ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {

    String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();

    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM/yyyy")));
    body.put("error", error);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }


  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<Object> handleDuplicateEmailException(
      DuplicateEmailException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    String date =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM/yyyy"));
    body.put("timestamp", date);
    body.put("error", ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DuplicateNameException.class)
  public ResponseEntity<Object> handleDuplicateNameException(
      DuplicateNameException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    String date =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm 'on' dd/MM/yyyy"));
    body.put("timestamp", date);
    body.put("error", ex.getMessage());

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }
}
