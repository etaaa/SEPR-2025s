package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.Collection;

/**
 * Data Access Object for owners.
 * Implements CRUD functionality for managing owners in the persistent data store.
 */
public interface OwnerDao {
  /**
   * Fetch an owner from the persistent data store by its ID.
   *
   * @param id the ID of the owner to get
   * @return the owner with the ID {@code id}
   * @throws NotFoundException if no owner with the given ID exists in the persistent data store
   */
  Owner getById(long id) throws NotFoundException;

  /**
   * Search for owners matching the criteria in {@code searchParameters}.
   *
   * <p>
   * A owner is considered matched, if its name contains {@code searchParameters.name} as a substring.
   * The returned stream of owners never contains more than {@code searchParameters.limit} elements,
   * even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param searchParameters object containing the search parameters to match
   * @return a stream containing owners matching the criteria in {@code searchParameters}
   */
  Collection<Owner> search(OwnerSearchDto searchParameters);

}
