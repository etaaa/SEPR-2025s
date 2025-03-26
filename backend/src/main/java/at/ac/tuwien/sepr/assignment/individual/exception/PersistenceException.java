package at.ac.tuwien.sepr.assignment.individual.exception;

/**
 * Exception that signals a failure during data persistence operations.
 * Typically used to wrap low-level database or storage-related errors.
 */
public class PersistenceException extends RuntimeException {

  public PersistenceException(String message) {
    super(message);
  }

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

}
