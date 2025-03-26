package at.ac.tuwien.sepr.assignment.individual.service;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
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
   * Retrieves detailed information about a horse by its unique identifier.
   * Includes the horse's owner and immediate parents, but not grandparents.
   *
   * @param id the unique identifier of the horse to retrieve
   * @return a {@link HorseDetailDto} containing detailed information about the horse
   * @throws NotFoundException if no horse with the specified {@code id} exists in the persistent data store
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
   * Retrieves all horses from the persistent data store in a summarized format.
   *
   * @return a stream of {@link HorseListDto} objects representing all horses
   */
  Stream<HorseListDto> getAll();

  /**
   * Retrieves the family tree of a horse up to a specified depth.
   *
   * @param id    the unique identifier of the horse whose family tree is to be retrieved
   * @param depth the number of generations to include in the family tree (minimum 1, maximum 25)
   * @return a {@link HorseFamilyTreeDto} representing the horse's family tree
   * @throws NotFoundException   if no horse with the specified {@code id} exists in the persistent data store
   * @throws ValidationException if {@code depth} is invalid (e.g., less than 1 or greater than 25)
   */
  HorseFamilyTreeDto getFamilyTree(long id, int depth) throws NotFoundException, ValidationException;

  /**
   * Searches for horses based on specified search criteria.
   * Results are limited to {@code searchParameters.limit} entries.
   *
   * @param searchParameters the search criteria to apply
   * @return a stream of {@link HorseListDto} objects matching the search criteria
   * @throws ValidationException if {@code searchParameters} is invalid (e.g., limit is null or negative)
   */
  Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException;

  /**
   * Creates a new horse in the persistent data store with optional image data.
   *
   * @param horse the data transfer object containing the horse details to create
   * @param image the optional image data and MIME type for the horse, or null if no image is provided
   * @return a {@link HorseDetailDto} representing the newly created horse
   * @throws ValidationException if {@code horse} data is invalid (e.g., missing name, future birth date)
   * @throws ConflictException   if the creation conflicts with existing data (e.g., parent sex mismatch)
   */
  HorseDetailDto create(HorseCreateDto horse, HorseImageDto image) throws ValidationException, ConflictException;

  /**
   * Updates an existing horse in the persistent data store with optional image data.
   *
   * @param horse the data transfer object containing the updated horse details, including the ID
   * @param image the optional new image data and MIME type, or null if unchanged or to be deleted
   * @return a {@link HorseDetailDto} representing the updated horse
   * @throws NotFoundException   if no horse with the ID in {@code horse} exists in the persistent data store
   * @throws ValidationException if {@code horse} data is invalid (e.g., missing sex, description too long)
   * @throws ConflictException   if the update conflicts with existing data (e.g., changing sex of a parent)
   */
  HorseDetailDto update(HorseUpdateDto horse, HorseImageDto image) throws NotFoundException, ValidationException, ConflictException;

  /**
   * Deletes a horse from the persistent data store by its unique identifier.
   *
   * @param id the unique identifier of the horse to delete
   * @throws NotFoundException if no horse with the specified {@code id} exists in the persistent data store
   */
  void delete(long id) throws NotFoundException;
}