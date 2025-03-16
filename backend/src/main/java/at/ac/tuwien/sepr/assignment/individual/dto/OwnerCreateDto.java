package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * Represents a Data Transfer Object (DTO) for creating a new owner.
 * This record encapsulates the required fields for registering an owner in the system.
 */
public record OwnerCreateDto(
    String firstName,
    String lastName,
    String description
) {
}
