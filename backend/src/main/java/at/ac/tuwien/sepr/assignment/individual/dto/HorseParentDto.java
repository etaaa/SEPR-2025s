package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * Represents a Data Transfer Object (DTO) for a horse's parent information.
 * Contains the parent horse's ID and name.
 */
public record HorseParentDto(
    Long id,
    String name
) {
}
