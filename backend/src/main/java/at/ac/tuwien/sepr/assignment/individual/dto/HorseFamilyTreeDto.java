package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * Represents a Data Transfer Object (DTO) for a horse's family tree.
 * Includes horse details along with references to its mother and father.
 */
public record HorseFamilyTreeDto(
    Long id,
    String name,
    LocalDate dateOfBirth,
    HorseFamilyTreeDto mother,
    HorseFamilyTreeDto father
) {
}
