package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing owner-related operations.
 * Provides endpoints for searching and creating owners.
 */
@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";
  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }


  @GetMapping
  public Stream<OwnerDto> getAll() {

    LOG.info("GET " + BASE_PATH);

    return service.getAll();
  }

  /**
   * Searches for owners based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the owner search
   * @return a stream of {@link OwnerDto} matching the search criteria
   */
  @GetMapping("/search")
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters)
      throws ValidationException {

    LOG.info("GET " + BASE_PATH + " query parameters: {}", searchParameters);

    return service.search(searchParameters);
  }


  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public OwnerDto create(@RequestBody OwnerCreateDto toCreate)
      throws ConflictException, ValidationException {

    LOG.info("POST " + BASE_PATH + "/{}", toCreate);
    LOG.debug("Body of request:\n{}", toCreate);

    return service.create(toCreate);
  }
}
