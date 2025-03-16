package at.ac.tuwien.sepr.assignment.individual.rest;

/**
 * Represents a Data Transfer Object (DTO) for generic error messages returned in REST responses.
 *
 * <p>This record encapsulates a simple error message.</p>
 *
 * @param message the error message
 */
public record ErrorDto(
    String message
) {
}
