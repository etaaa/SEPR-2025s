package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailOwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service for working with owners.
 */
public interface OwnerService {

  /**
   * Retrieves an owner from the persistent data store by its unique identifier.
   *
   * @param id the unique identifier of the owner to retrieve
   * @return an {@link OwnerDto} representing the owner with the specified ID
   * @throws NotFoundException if no owner with the given {@code id} exists in the persistent data store
   */
  OwnerDto getById(long id) throws NotFoundException;

  /**
   * Retrieves all owners from the persistent data store.
   *
   * @return a stream of {@link OwnerDto} objects representing all owners
   */
  Stream<OwnerDto> getAll();

  /**
   * Retrieves multiple owners by their unique identifiers.
   *
   * @param ids a collection of owner IDs to retrieve
   * @return a map of owner IDs to {@link HorseDetailOwnerDto} objects containing the requested owners
   * @throws NotFoundException if any owner with an ID in {@code ids} is not found in the persistent data store
   */
  Map<Long, HorseDetailOwnerDto> getAllById(Collection<Long> ids) throws NotFoundException;

  /**
   * Searches for owners based on specified search criteria.
   * An owner matches if its name contains {@code searchParameters.name} as a substring (case-sensitive).
   * The result is limited to {@code searchParameters.limit} entries.
   *
   * @param searchParameters the search criteria to apply
   * @return a stream of {@link OwnerDto} objects matching the search criteria
   * @throws ValidationException if {@code searchParameters} is invalid (e.g., limit is null or negative)
   */
  Stream<OwnerDto> search(OwnerSearchDto searchParameters) throws ValidationException;

  /**
   * Creates a new owner in the persistent data store.
   *
   * @param owner the data transfer object containing the owner details to create
   * @return an {@link OwnerDto} representing the newly created owner
   * @throws ValidationException if {@code owner} data is invalid (e.g., missing first name, description too long)
   * @throws ConflictException   if the creation conflicts with existing data (e.g., system-specific constraints)
   */
  OwnerDto create(OwnerCreateDto owner) throws ValidationException, ConflictException;
}