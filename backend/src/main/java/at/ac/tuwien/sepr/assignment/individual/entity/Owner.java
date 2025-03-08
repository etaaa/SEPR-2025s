package at.ac.tuwien.sepr.assignment.individual.entity;

/**
 * Represents an owner in the persistent data store.
 */
public record Owner(
    Long id,
    String firstName,
    String lastName,
    String email
) {
}
