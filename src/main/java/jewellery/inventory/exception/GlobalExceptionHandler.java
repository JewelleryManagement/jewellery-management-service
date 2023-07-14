package jewellery.inventory.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    Map<String, Object> body = new HashMap<>();
    String date =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("'at' HH:mm 'on' dd/MM/yyyy"));
    body.put("timestamp", date);
    body.put("errors", errors);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, WebRequest request) {
    Map<String, Object> body = new HashMap<>();
    String date =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("'at' HH:mm 'on' dd/MM/yyyy"));
    body.put("timestamp", date);

    // Customize the error message as per your requirement
    if (ex.getCause() instanceof ConstraintViolationException cause) {
      String constraintName = cause.getConstraintName();
      System.out.println("constraintName: " + constraintName);
      if (constraintName.contains("email")) {
        body.put("error", "Email already exists");
      } else if (constraintName.contains("name")) {
        body.put("error", "Name already exists");
      } else {
        body.put("error", "Data integrity violation");
      }
    } else {
      body.put("error", "Data integrity violation");
    }

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }
}
