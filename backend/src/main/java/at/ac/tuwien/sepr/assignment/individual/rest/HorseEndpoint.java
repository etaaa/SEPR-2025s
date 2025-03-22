package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateRestDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing horse-related operations.
 * Provides endpoints for searching, retrieving, creating, updating, and deleting horses,
 * as well as fetching their family tree.
 */
@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";

  private final HorseService service;

  @Autowired
  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  /**
   * Retrieves the details of a horse by its ID.
   *
   * @param id the unique identifier of the horse
   * @return the detailed information of the requested horse
   * @throws ResponseStatusException if the horse is not found
   */
  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Retrieves the image associated with a horse.
   *
   * @param id the unique identifier of the horse whose image is requested
   * @return a {@link ResponseEntity} containing the image as a byte array along with the proper MIME type header
   * @throws ResponseStatusException with HTTP status 404 if the image or the horse is not found
   */
  @GetMapping("/{id}/image")
  public ResponseEntity<byte[]> getHorseImage(
      @PathVariable("id") long id) {

    LOG.info("GET image for horse with id {}", id);

    try {
      HorseImageDto horseImageDto = service.getImageById(id);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(horseImageDto.mimeType()));
      return new ResponseEntity<>(horseImageDto.image(), headers, HttpStatus.OK);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Image for horse with ID " + id + " not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }

  }


  @GetMapping
  public Stream<HorseListDto> getAll() {
    LOG.info("GET " + BASE_PATH);
    return service.getAll();
  }

  /**
   * Searches for horses based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the horse search
   * @return a stream of {@link HorseListDto} matching the search criteria
   */
  @GetMapping("/search")
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);
    System.out.println(searchParameters);
    return service.search(searchParameters);
  }


  @GetMapping("/{id}/familytree")
  public HorseFamilyTreeDto getFamilyTree(
      @PathVariable("id") long id,
      @RequestParam(name = "generations", defaultValue = "1") int generations) {

    LOG.info("GET " + BASE_PATH + "/{}/familytree?generations={}", id, generations);

    try {
      return service.getFamilyTree(id, generations);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get family tree of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ValidationException e) {
      HttpStatus status = HttpStatus.BAD_REQUEST;
      logClientError(status, "Invalid generations parameter", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Creates a new horse with the provided details and an optional image.
   *
   * @param toCreate the data transfer object containing horse details for creation
   * @param image    an optional image file for the horse; may be null or empty
   * @return the details of the newly created horse as a {@link HorseDetailDto}
   * @throws ResponseStatusException with HTTP status 400 if image processing or validation fails,
   *                                 or with HTTP status 409 if there is a conflict with existing data.
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public HorseDetailDto create(
      @ModelAttribute HorseCreateDto toCreate,
      @RequestParam(value = "image", required = false) MultipartFile image)
      throws ValidationException, ConflictException {

    LOG.info("POST " + BASE_PATH + "/{}", toCreate);
    LOG.debug("Body of request:\n{}", toCreate);

    HorseImageDto horseImage = null;
    if (image != null && !image.isEmpty()) {
      try {
        horseImage = new HorseImageDto(image.getBytes(), image.getContentType());
      } catch (IOException e) {
        LOG.error("Error while processing the image", e);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while processing the image", e);
      }
    }

    try {
      return service.create(toCreate, horseImage);
    } catch (ValidationException e) {
      HttpStatus status = HttpStatus.BAD_REQUEST;
      logClientError(status, "Validation of Horse failed", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "Conflict with existing data", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }

  }

  /**
   * Updates the details of an existing horse, including an optional image file.
   *
   * @param id       the unique identifier of the horse to update
   * @param toUpdate the data transfer object containing updated horse details
   * @param image    an optional new image file for the horse; may be null or empty
   * @return the updated horse details as a {@link HorseDetailDto}
   * @throws ResponseStatusException with HTTP status 404 if the horse is not found,
   *                                 with HTTP status 400 if validation fails or image processing fails,
   *                                 or with HTTP status 409 if there is a conflict with existing data.
   */
  @PutMapping(path = "{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public HorseDetailDto update(
      @PathVariable("id") long id,
      @ModelAttribute HorseUpdateRestDto toUpdate,
      @RequestParam(value = "image", required = false) MultipartFile image)
      throws ValidationException, ConflictException {

    LOG.info("PUT " + BASE_PATH + "/{}", toUpdate);
    LOG.debug("Body of request:\n{}", toUpdate);

    HorseImageDto horseImage = null;
    if (image != null && !image.isEmpty()) {
      try {
        horseImage = new HorseImageDto(image.getBytes(), image.getContentType());
      } catch (IOException e) {
        LOG.error("Error while processing the image", e);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while processing the image", e);
      }
    }

    try {
      return service.update(toUpdate.toUpdateDtoWithId(id), horseImage);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ValidationException e) {
      HttpStatus status = HttpStatus.BAD_REQUEST;
      logClientError(status, "Validation of Horse failed", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "Conflict with existing data", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }

  }

  /**
   * Deletes the horse with the given unique identifier.
   *
   * @param id the unique identifier of the horse to delete
   * @throws ResponseStatusException with HTTP status 404 if the horse is not found
   */
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") long id) {
    LOG.info("DELETE " + BASE_PATH + "/{}", id);
    try {
      service.delete(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to delete not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Logs client-side errors with relevant details.
   *
   * @param status  the HTTP status code of the error
   * @param message a brief message describing the error
   * @param e       the exception that occurred
   */
  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }

}
