package at.ac.tuwien.sepr.assignment.individual.dto;

import java.util.Collection;

/**
 * DTO to encapsulate parameters for Owner search.
 * An owner, whose name has {@code name} as a substring is considered matched.
 *
 * @param name      substring of the owner's name
 * @param limit the maximum number of owners to return, even if there are more matches
 */
public record OwnerSearchDto(
    String name,
    Collection<Long> ids,
    Integer limit // needs to be present always
) {
}
