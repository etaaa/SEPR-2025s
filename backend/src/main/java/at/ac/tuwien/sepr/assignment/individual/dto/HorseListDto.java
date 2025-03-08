package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;

/**
 * Represents a Data Transfer Object (DTO) for loading a list of horses.
 * This record encapsulates essential horse attributes required for listing.
 */
public record HorseListDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner
) {
}
