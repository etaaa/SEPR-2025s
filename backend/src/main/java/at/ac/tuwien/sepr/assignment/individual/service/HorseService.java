package at.ac.tuwien.sepr.assignment.individual.service;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {

  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Retrieves the image associated with a horse by its unique identifier.
   *
   * @param id the unique identifier of the horse whose image is to be retrieved
   * @return a {@link HorseImageDto} containing the image data and MIME type
   * @throws NotFoundException if no horse with the specified {@code id} exists or if it has no associated image
   */
  HorseImageDto getImageById(long id) throws NotFoundException;

  /**
   * Lists all horses stored in the system.
   *
   * @return list of all stored horses
   */
  Stream<HorseListDto> allHorses();

  /**
   * Creates a new horse entry in the system with the provided data and optional image.
   *
   * @param horse the {@link HorseCreateDto} containing the horse's creation details
   * @param image the {@link HorseImageDto} containing the optional image data and MIME type, or null if no image is provided
   * @return a {@link HorseDetailDto} representing the newly created horse
   * @throws ValidationException if the provided {@code horse} data is invalid (e.g., missing required fields, invalid dates)
   * @throws ConflictException   if the creation conflicts with existing data (e.g., duplicate horse identifiers)
   */
  HorseDetailDto create(HorseCreateDto horse, HorseImageDto image) throws ValidationException, ConflictException;

  /**
   * Updates an existing horse in the system with the provided data and optional image.
   *
   * @param horse the {@link HorseUpdateDto} containing the updated horse details, including the ID of the horse to update
   * @param image the {@link HorseImageDto} containing the new image data and MIME type, or null if the image is unchanged or to be deleted
   * @return a {@link HorseDetailDto} representing the updated horse
   * @throws NotFoundException   if no horse with the ID specified in {@code horse} exists in the persistent data store
   * @throws ValidationException if the provided {@code horse} data is invalid (e.g., missing required fields, description too long)
   * @throws ConflictException   if the update conflicts with existing data (e.g., owner does not exist)
   */
  HorseDetailDto update(HorseUpdateDto horse, HorseImageDto image) throws NotFoundException, ValidationException, ConflictException;

  /**
   * Deletes a horse from the system by its unique identifier.
   *
   * @param id the unique identifier of the horse to delete
   * @throws NotFoundException if no horse with the specified {@code id} exists in the persistent data store
   */
  void delete(long id) throws NotFoundException;

}
