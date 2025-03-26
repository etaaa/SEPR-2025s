package at.ac.tuwien.sepr.assignment.individual.rest;


import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.PersistenceException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Central exception handler for REST controllers, mapping exceptions to HTTP responses.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Handles {@link MethodArgumentTypeMismatchException} by returning a 400 Bad Request response.
   *
   * @param e the exception indicating a type mismatch in a method argument
   * @return an {@link ErrorDto} containing the error message about the invalid parameter
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

    String paramName = e.getName();
    String value = e.getValue() != null ? e.getValue().toString() : "null";
    String typeName = e.getRequiredType() != null ? e.getRequiredType().getSimpleName().toLowerCase() : "value";
    String message = String.format("Parameter '%s' with value '%s' must be a valid %s", paramName, value, typeName);

    LOG.warn("Invalid parameter [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto(message);
  }

  /**
   * Handles {@link MethodArgumentNotValidException} by returning a 400 Bad Request response.
   *
   * @param e the exception indicating validation failure of request data
   * @return an {@link ErrorDto} containing a summary of field validation errors
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
    StringBuilder errorMessage = new StringBuilder("Request data validation failed: ");
    for (FieldError error : fieldErrors) {
      errorMessage.append(String.format("Field '%s' %s; ", error.getField(), error.getDefaultMessage()));
    }
    String message = errorMessage.toString();

    LOG.warn("Request data validation failed [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto(message);
  }

  /**
   * Handles {@link StackOverflowError} by returning a 400 Bad Request response.
   *
   * @param e the error indicating excessive recursion due to a large generations value
   * @return an {@link ErrorDto} indicating the system capacity was exceeded
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleStackOverflowError(StackOverflowError e) {

    String message = "Requested generations value is too large and exceeds system capacity";

    LOG.warn("Excessive recursion [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto(message);
  }

  /**
   * Handles {@link HttpMessageNotReadableException} by returning a 400 Bad Request response.
   *
   * @param e the exception indicating malformed JSON in the request
   * @return an {@link ErrorDto} describing the JSON format error
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

    String message = "Invalid JSON format: " + e.getLocalizedMessage();

    LOG.warn("Malformed JSON [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto(message);
  }

  /**
   * Handles {@link NotFoundException} by returning a 404 Not Found response.
   *
   * @param e the exception indicating a requested resource was not found
   * @return an {@link ErrorDto} containing the not found error message
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public ErrorDto handleNotFoundException(NotFoundException e) {

    LOG.warn("Resource not found [requestId={}]: {}", MDC.get("r"), e.getMessage());

    return new ErrorDto(e.getMessage());
  }

  /**
   * Handles {@link NoHandlerFoundException} by returning a 404 Not Found response.
   *
   * @param e the exception indicating no handler exists for the requested URL
   * @return an {@link ErrorDto} indicating the resource or page does not exist
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public ErrorDto handleNoHandlerFoundException(NoHandlerFoundException e) {

    String message = "The requested page or resource does not exist: " + e.getRequestURL();

    LOG.warn("Page not found [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto("The requested page or resource does not exist.");
  }

  /**
   * Handles {@link HttpRequestMethodNotSupportedException} by returning a 405 Method Not Allowed response.
   *
   * @param e the exception indicating an unsupported HTTP method was used
   * @return an {@link ErrorDto} describing the unsupported method error
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ResponseBody
  public ErrorDto handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {

    String message = "Method " + e.getMethod() + " is not supported for this resource.";

    LOG.warn("Method not allowed [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto(message);
  }

  /**
   * Handles {@link ConflictException} by returning a 409 Conflict response.
   *
   * @param e the exception indicating a conflict with existing data
   * @return an {@link ErrorDto} containing the conflict error message
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public ErrorDto handleConflictException(ConflictException e) {

    LOG.warn("Conflict with existing data [requestId={}]: {}", MDC.get("r"), e.getMessage());

    return new ErrorDto(e.getMessage());
  }

  /**
   * Handles {@link MaxUploadSizeExceededException} by returning a 413 Payload Too Large response.
   *
   * @param e the exception indicating the uploaded file exceeds the maximum size
   * @return an {@link ErrorDto} describing the upload size limit exceeded error
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  @ResponseBody
  public ErrorDto handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {

    String message = "The uploaded file exceeds the maximum allowed size: " + e.getMaxUploadSize();

    LOG.warn("Upload size exceeded [requestId={}]: {}", MDC.get("r"), message);

    return new ErrorDto("The uploaded file exceeds the maximum allowed size.");
  }

  /**
   * Handles {@link ValidationException} by returning a 422 Unprocessable Entity response.
   *
   * @param e the exception indicating validation failure of business logic constraints
   * @return a {@link ValidationErrorRestDto} containing validation error summary and details
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ValidationErrorRestDto handleValidationException(ValidationException e) {

    LOG.warn("Validation failed [requestId={}]: {}", MDC.get("r"), e.summary());

    return new ValidationErrorRestDto(e.summary(), e.errors());
  }

  /**
   * Handles {@link PersistenceException} by returning a 500 Internal Server Error response.
   *
   * @param e the exception indicating a database access failure
   * @return an {@link ErrorDto} indicating a server error occurred
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorDto handlePersistenceException(PersistenceException e) {

    LOG.error("Database access failed [requestId={}]: {}", MDC.get("r"), e.getMessage(), e);

    return new ErrorDto("A server error occurred while accessing the database.");
  }

  /**
   * Handles {@link IOException} by returning a 500 Internal Server Error response.
   *
   * @param e the exception indicating an I/O error during request processing
   * @return an {@link ErrorDto} describing the I/O error
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorDto handleIOException(IOException e) {

    LOG.error("IO error [requestId={}]: {}", MDC.get("r"), e.getMessage(), e);

    return new ErrorDto("An error occurred while processing the request: " + e.getMessage());
  }

  /**
   * Handles any uncaught {@link Exception} by returning a 500 Internal Server Error response.
   *
   * @param e the generic exception indicating an unexpected server error
   * @return an {@link ErrorDto} indicating an unexpected error occurred
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorDto handleGenericException(Exception e) {

    LOG.error("Unexpected error [requestId={}]: {}", MDC.get("r"), e.getMessage(), e);

    return new ErrorDto("An unexpected server error occurred.");
  }
}
