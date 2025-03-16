package at.ac.tuwien.sepr.assignment.individual.rest;


import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Central exception handler for REST controllers, mapping exceptions to HTTP responses.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Handles {@link ValidationException} by returning a 422 Unprocessable Entity response.
   *
   * @param e the validation exception
   * @return a {@link ValidationErrorRestDto} containing validation error details
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ValidationErrorRestDto handleValidationException(ValidationException e) {
    LOG.warn("Terminating request processing with status 422 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
    return new ValidationErrorRestDto(e.summary(), e.errors());
  }

  /**
   * Handles {@link MaxUploadSizeExceededException} and returns a response with HTTP status 413 (Payload Too Large).
   *
   * @param e the exception indicating that the uploaded file exceeds the maximum allowed size.
   * @return an {@link ErrorDto} containing a generic error message.
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ErrorDto handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    LOG.warn("Upload size exceeded: {}", e.getMessage());
    return new ErrorDto("The uploaded file exceeds the maximum allowed size.");
  }

}
