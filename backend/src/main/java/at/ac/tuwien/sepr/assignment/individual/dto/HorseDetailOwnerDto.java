package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * Represents a Data Transfer Object (DTO) for horse owner details.
 * Contains the first and last name of the horse's owner.
 */
public record HorseDetailOwnerDto(
    String firstName,
    String lastName
) {
}
