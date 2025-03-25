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
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

  /**
   * Searches for owners based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the owner search
   * @return a stream of {@link OwnerDto} matching the search criteria
   */
  @GetMapping
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) throws ValidationException {

    if (searchParameters.isEmpty()) {
      LOG.info("Processing GET {} request [requestId={}]: Retrieving all owners", BASE_PATH, MDC.get("r"));

      return service.getAll();
    }

    LOG.info("Processing GET {} request [requestId={}]: Searching owners with parameters {}", BASE_PATH, MDC.get("r"), searchParameters);

    return service.search(searchParameters);
  }


  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public OwnerDto create(@RequestBody OwnerCreateDto toCreate) throws ConflictException, ValidationException {

    LOG.info("Processing POST {} request [requestId={}]: Creating owner with data {}", BASE_PATH, MDC.get("r"), toCreate);
    LOG.debug("Create request details [requestId={}]: Owner data {}", MDC.get("r"), toCreate);

    OwnerDto createdOwner = service.create(toCreate);

    LOG.info("Successfully created owner with id {} [requestId={}]", createdOwner.id(), MDC.get("r"));

    return createdOwner;
  }
}
