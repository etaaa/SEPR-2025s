package at.ac.tuwien.sepr.assignment.individual.rest;


import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.PersistenceException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Central exception handler for REST controllers, mapping exceptions to HTTP responses.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    String paramName = e.getName();
    String value = e.getValue() != null ? e.getValue().toString() : "null";
    String typeName = e.getRequiredType() != null ? e.getRequiredType().getSimpleName().toLowerCase() : "value";
    String message = String.format("Parameter '%s' with value '%s' must be a valid %s", paramName, value, typeName);
    LOG.warn("400 Invalid parameter [r={}]: {}", MDC.get("r"), message);
    return new ErrorDto(message);
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleStackOverflowError(StackOverflowError e) {
    LOG.warn("400 Excessive recursion [r={}]: {}: {}", MDC.get("r"), e.getClass().getSimpleName(), e.getMessage());
    return new ErrorDto("Requested generations value is too large and exceeds system capacity");
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorDto handleIOException(IOException e) {
    LOG.warn("400 IO error [r={}]: {}: {}", MDC.get("r"), e.getClass().getSimpleName(), e.getMessage());
    return new ErrorDto("An error occurred while processing the request: " + e.getMessage());
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public ErrorDto handleNotFoundException(NotFoundException e) {
    LOG.warn("404 Resource not found [r={}]: {}: {}", MDC.get("r"), e.getClass().getSimpleName(), e.getMessage());
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public ErrorDto handleConflictException(ConflictException e) {
    LOG.warn("409 Conflict with existing data [r={}]: {}: {}", MDC.get("r"), e.getClass().getSimpleName(), e.getMessage());
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  @ResponseBody
  public ErrorDto handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    LOG.warn("413 Upload size exceeded [r={}]: {}: {}", MDC.get("r"), e.getClass().getSimpleName(), e.getMessage());
    return new ErrorDto("The uploaded file exceeds the maximum allowed size.");
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ValidationErrorRestDto handleValidationException(ValidationException e) {
    LOG.warn("422 Validation failed [r={}]: {}: {}", MDC.get("r"), e.getClass().getSimpleName(), e.getMessage());
    return new ValidationErrorRestDto(e.summary(), e.errors());
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorDto handlePersistenceException(PersistenceException e) {
    /*
    Log the full stack trace for unexpected errors as specified
    in Techstory 10.
     */
    LOG.error("500 Database access failed [r={}]: {}", MDC.get("r"), e.getMessage(), e);
    return new ErrorDto("A server error occurred while accessing the database.");
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorDto handleGenericException(Exception e) {
    /*
    Log the full stack trace for unexpected errors as specified
    in Techstory 10.
     */
    LOG.error("500 Unexpected error [r={}]: {}: {}", MDC.get("r"), e.getClass().getName(), e.getMessage(), e);
    return new ErrorDto("An unexpected server error occurred.");
  }
}
