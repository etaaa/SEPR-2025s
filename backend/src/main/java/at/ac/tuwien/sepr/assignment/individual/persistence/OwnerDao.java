package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.List;

/**
 * Data Access Object for owners.
 * Implements CRUD functionality for managing owners in the persistent data store.
 */
public interface OwnerDao {

  /**
   * Retrieves an owner from the persistent data store by its unique identifier.
   *
   * @param id the unique identifier of the owner to retrieve
   * @return an {@link Owner} entity with the specified {@code id}
   * @throws NotFoundException if no owner with the given {@code id} exists in the persistent data store
   */
  Owner getById(long id) throws NotFoundException;

  /**
   * Retrieves all owners from the persistent data store.
   *
   * @return a list of all {@link Owner} entities
   */
  List<Owner> getAll();

  /**
   * Searches for owners based on specified search criteria.
   * An owner matches if its full name (first name + last name) contains {@code searchParameters.name} as a substring (case-insensitive).
   * The result is limited to {@code searchParameters.limit} entries.
   *
   * @param searchParameters the search criteria to apply
   * @return a list of {@link Owner} entities matching the search criteria
   */
  List<Owner> search(OwnerSearchDto searchParameters);

  /**
   * Creates a new owner in the persistent data store.
   *
   * @param owner the data transfer object containing the owner details to create
   * @return an {@link Owner} entity representing the newly created owner, including its generated ID
   */
  Owner create(OwnerCreateDto owner);
}
