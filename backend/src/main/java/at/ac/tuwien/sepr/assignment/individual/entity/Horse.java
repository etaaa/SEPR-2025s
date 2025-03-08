package at.ac.tuwien.sepr.assignment.individual.entity;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;

/**
 * Represents a horse in the persistent data store.
 */
public record Horse(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    Long ownerId
) {
}
