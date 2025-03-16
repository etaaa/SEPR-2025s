package at.ac.tuwien.sepr.assignment.individual.persistence;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
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
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Retrieves the image associated with a horse by its unique identifier.
   *
   * @param id the unique identifier of the horse whose image is to be retrieved
   * @return the {@link HorseImageDto} containing the image data and MIME type
   * @throws NotFoundException if no horse or image with the given ID exists in the persistent data store
   */
  HorseImageDto getImageById(long id) throws NotFoundException;

  List<Horse> search(HorseSearchDto searchParameters);

  /**
   * Creates a new horse in the persistent data store.
   *
   * @param horse      the {@link HorseCreateDto} containing details for the new horse
   * @param horseImage the {@link HorseImageDto} containing image data for the new horse; may be null if no image is provided
   * @return the newly created {@link Horse} entity
   */
  Horse create(HorseCreateDto horse, HorseImageDto horseImage);

  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseUpdateDto horse, HorseImageDto horseImage) throws NotFoundException;

  /**
   * Deletes a horse by its unique identifier from the persistent data store.
   *
   * @param id the unique identifier of the horse to delete
   * @throws NotFoundException if no horse with the given ID exists in the persistent data store
   */
  void delete(long id) throws NotFoundException;

}
