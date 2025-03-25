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
import org.slf4j.MDC;
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
  public HorseDetailDto getById(@PathVariable("id") long id) throws NotFoundException {

    LOG.info("Processing GET {} request [requestId={}]: Retrieving horse with id {}", BASE_PATH + "/{}", MDC.get("r"), id);

    return service.getById(id);
  }

  /**
   * Retrieves the image associated with a horse.
   *
   * @param id the unique identifier of the horse whose image is requested
   * @return a {@link ResponseEntity} containing the image as a byte array along with the proper MIME type header
   * @throws ResponseStatusException with HTTP status 404 if the image or the horse is not found
   */
  @GetMapping("/{id}/image")
  public ResponseEntity<byte[]> getHorseImage(@PathVariable("id") long id) throws NotFoundException {

    LOG.info("Processing GET {}/image request [requestId={}]: Retrieving image for horse with id {}", BASE_PATH + "/{}", MDC.get("r"), id);

    HorseImageDto horseImageDto = service.getImageById(id);

    LOG.debug("Retrieved image for horse id {} [requestId={}]: MIME type {}", id, MDC.get("r"), horseImageDto.mimeType());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(horseImageDto.mimeType()));

    return new ResponseEntity<>(horseImageDto.image(), headers, HttpStatus.OK);
  }

  /**
   * Searches for horses based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the horse search
   * @return a stream of {@link HorseListDto} matching the search criteria
   */
  @GetMapping
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException {

    if (searchParameters.isEmpty()) {
      LOG.info("Processing GET {} request [requestId={}]: Retrieving all horses", BASE_PATH, MDC.get("r"));

      return service.getAll();
    }

    LOG.info("Processing GET {} request [requestId={}]: Searching horses with parameters {}", BASE_PATH, MDC.get("r"), searchParameters);

    return service.search(searchParameters);
  }


  @GetMapping("/{id}/familytree")
  public HorseFamilyTreeDto getFamilyTree(@PathVariable("id") long id, @RequestParam(name = "generations", defaultValue = "1") int generations)
      throws NotFoundException, ValidationException {

    LOG.info("Processing GET {}/familytree request [requestId={}]: Retrieving family tree for horse id {} with {} generations", BASE_PATH + "/{}", MDC.get("r"),
        id, generations);

    return service.getFamilyTree(id, generations);
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
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto create(@ModelAttribute HorseCreateDto toCreate, @RequestParam(value = "image", required = false) MultipartFile image)
      throws ValidationException, ConflictException, IOException {

    LOG.info("Processing POST {} request [requestId={}]: Creating horse with data {}", BASE_PATH, MDC.get("r"), toCreate);
    LOG.debug("Create request details [requestId={}]: Horse data {}, Image provided: {}", MDC.get("r"), toCreate, image != null && !image.isEmpty());

    HorseImageDto horseImage = null;
    if (image != null && !image.isEmpty()) {
      LOG.trace("Processing image upload for new horse [requestId={}]: Size {} bytes, MIME type {}", MDC.get("r"), image.getSize(), image.getContentType());

      horseImage = new HorseImageDto(image.getBytes(), image.getContentType());
    }

    HorseDetailDto createdHorse = service.create(toCreate, horseImage);

    LOG.info("Successfully created horse with id {} [requestId={}]", createdHorse.id(), MDC.get("r"));

    return createdHorse;
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
  public HorseDetailDto update(@PathVariable("id") long id, @ModelAttribute HorseUpdateRestDto toUpdate,
                               @RequestParam(value = "image", required = false) MultipartFile image)
      throws NotFoundException, ValidationException, ConflictException, IOException {

    LOG.info("Processing PUT {}/{} request [requestId={}]: Updating horse with id {} and data {}",
        BASE_PATH, id, MDC.get("r"), id, toUpdate);
    LOG.debug("Update request details [requestId={}]: Horse data {}, Image provided: {}", MDC.get("r"), toUpdate, image != null && !image.isEmpty());

    HorseImageDto horseImage = null;
    if (image != null && !image.isEmpty()) {
      LOG.trace("Processing image upload for horse id {} [requestId={}]: Size {} bytes, MIME type {}",
          id, MDC.get("r"), image.getSize(), image.getContentType());

      horseImage = new HorseImageDto(image.getBytes(), image.getContentType());
    }

    HorseDetailDto updatedHorse = service.update(toUpdate.toUpdateDtoWithId(id), horseImage);
    LOG.info("Successfully updated horse with id {} [requestId={}]", id, MDC.get("r"));
    return updatedHorse;
  }

  /**
   * Deletes the horse with the given unique identifier.
   *
   * @param id the unique identifier of the horse to delete
   * @throws ResponseStatusException with HTTP status 404 if the horse is not found
   */
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") long id) throws NotFoundException {

    LOG.info("Processing DELETE {}/{} request [requestId={}]: Deleting horse with id {}", BASE_PATH, id, MDC.get("r"), id);

    service.delete(id);

    LOG.info("Successfully deleted horse with id {} [requestId={}]", id, MDC.get("r"));
  }
}
