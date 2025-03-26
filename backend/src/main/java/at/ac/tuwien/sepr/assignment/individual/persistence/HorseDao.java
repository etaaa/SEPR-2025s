package at.ac.tuwien.sepr.assignment.individual.persistence;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;

import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {

  /**
   * Retrieves a horse by its unique identifier from the persistent data store.
   *
   * @param id the unique identifier of the horse to retrieve
   * @return the {@link Horse} entity with the specified ID
   * @throws NotFoundException if no horse with the given {@code id} exists in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Retrieves the image associated with a horse by its unique identifier.
   *
   * @param id the unique identifier of the horse whose image is to be retrieved
   * @return a {@link HorseImageDto} containing the image data and MIME type
   * @throws NotFoundException if no horse with the given {@code id} exists or it has no associated image
   */
  HorseImageDto getImageById(long id) throws NotFoundException;

  /**
   * Retrieves a horse's parent details by its unique identifier.
   *
   * @param id the unique identifier of the parent horse to retrieve
   * @return a {@link HorseParentDto} containing the parent's ID and name
   * @throws NotFoundException if no horse with the given {@code id} exists in the persistent data store
   */
  HorseParentDto getParentById(long id) throws NotFoundException;

  /**
   * Retrieves all children of a horse by its unique identifier.
   *
   * @param id the unique identifier of the parent horse whose children are to be retrieved
   * @return a list of {@link Horse} entities representing the children
   */
  List<Horse> getChildrenByParentId(long id);

  /**
   * Retrieves all horses from the persistent data store.
   *
   * @return a list of {@link Horse} entities representing all horses
   */
  List<Horse> getAll();

  /**
   * Searches for horses based on specified search criteria.
   *
   * @param searchParameters the criteria to filter horses (e.g., name, sex, date of birth)
   * @return a list of {@link Horse} entities matching the search criteria
   */
  List<Horse> search(HorseSearchDto searchParameters);

  /**
   * Creates a new horse in the persistent data store.
   *
   * @param horse      the data transfer object containing the details for the new horse
   * @param horseImage the optional image data and MIME type for the horse, or null if no image is provided
   * @return the newly created {@link Horse} entity
   */
  Horse create(HorseCreateDto horse, HorseImageDto horseImage);

  /**
   * Updates an existing horse in the persistent data store.
   *
   * @param horse      the data transfer object containing the updated horse details, including the ID
   * @param horseImage the optional new image data and MIME type, or null if unchanged or to be deleted
   * @return the updated {@link Horse} entity
   * @throws NotFoundException if no horse with the ID in {@code horse} exists in the persistent data store
   */
  Horse update(HorseUpdateDto horse, HorseImageDto horseImage) throws NotFoundException;

  /**
   * Deletes a horse from the persistent data store by its unique identifier.
   *
   * @param id the unique identifier of the horse to delete
   * @throws NotFoundException if no horse with the given {@code id} exists in the persistent data store
   */
  void delete(long id) throws NotFoundException;
}