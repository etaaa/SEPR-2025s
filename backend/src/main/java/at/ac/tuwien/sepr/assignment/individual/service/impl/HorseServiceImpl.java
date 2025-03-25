package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailOwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.PersistenceException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link HorseService} for handling image storage and retrieval.
 */
@Service
public class HorseServiceImpl implements HorseService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  @Autowired
  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {

    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {

    /*
    In this method we won't have to validate the ID (check if there
    exists a horse with the given ID), as that would be useless. We
    would make the same query twice in that case (see
    https://tuwel.tuwien.ac.at/mod/forum/discuss.php?d=478592).
     */
    LOG.trace("Entering getById [requestId={}]: Retrieving horse with id {}", MDC.get("r"), id);

    Horse horse = dao.getById(id);

    LOG.debug("Retrieved horse with id {} [requestId={}]: {}", id, MDC.get("r"), horse);

    HorseParentDto mother = null;
    HorseParentDto father = null;
    if (horse.motherId() != null) {
      try {
        mother = dao.getParentById(horse.motherId());

        LOG.debug("Retrieved mother for horse id {} [requestId={}]: id {}", id, MDC.get("r"), horse.motherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Mother with ID {} not found for horse id {}, although it was validated",
            MDC.get("r"), horse.motherId(), id, e);

        throw new FatalException("Mother with ID " + horse.motherId() + " not found, although it was validated.", e);
      }
    }
    if (horse.fatherId() != null) {
      try {
        father = dao.getParentById(horse.fatherId());

        LOG.debug("Retrieved father for horse id {} [requestId={}]: id {}", id, MDC.get("r"), horse.fatherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Father with ID {} not found for horse id {}, although it was validated",
            MDC.get("r"), horse.fatherId(), id, e);

        throw new FatalException("Father with ID " + horse.fatherId() + " not found, although it was validated.", e);
      }
    }

    HorseDetailDto result = mapper.entityToDetailDto(horse, ownerMapForSingleId(horse.ownerId()), mother, father);

    LOG.info("Successfully retrieved horse with id {} [requestId={}]", id, MDC.get("r"));

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseImageDto getImageById(long id) throws NotFoundException {

    /*
     The recursive family tree logic is implemented here in the service
     layer to keep the persistence layer focused on simple data access.
     This follows clean architecture principles by separating business
     logic from database queries, improves testability, and avoids
     database-specific SQL features.
     */
    LOG.trace("Entering getImageById [requestId={}]: Retrieving image for horse with id {}", MDC.get("r"), id);

    HorseImageDto image = dao.getImageById(id);

    LOG.debug("Retrieved image for horse id {} [requestId={}]: MIME type {}", id, MDC.get("r"), image.mimeType());

    return image;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<HorseListDto> getAll() {

    LOG.trace("Entering getAll [requestId={}]: Retrieving all horses", MDC.get("r"));

    var horses = dao.getAll();

    LOG.debug("Retrieved {} horses [requestId={}]", horses.size(), MDC.get("r"));

    var ownerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());

    Map<Long, HorseDetailOwnerDto> ownerMap;

    try {
      ownerMap = ownerService.getAllById(ownerIds);

      LOG.debug("Retrieved owners for {} IDs [requestId={}]", ownerIds.size(), MDC.get("r"));

    } catch (NotFoundException e) {
      LOG.error("Unexpected error [requestId={}]: Horse refers to non-existing owner", MDC.get("r"), e);

      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }

    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseFamilyTreeDto getFamilyTree(long id, int depth) throws NotFoundException, ValidationException {

    LOG.trace("Entering getFamilyTree [requestId={}]: Retrieving family tree for horse id {} with depth {}", MDC.get("r"), id, depth);

    validator.validateGenerations(depth);

    Horse horse = dao.getById(id);
    HorseFamilyTreeDto tree = buildFamilyTree(horse, depth);

    LOG.info("Successfully built family tree for horse id {} with depth {} [requestId={}]", id, depth, MDC.get("r"));

    return tree;
  }

  /**
   * Recursively constructs a family tree for a horse up to the specified depth.
   * Fetches parent information from the persistence layer and builds a tree structure.
   *
   * @param horse the horse entity to build the family tree for, or null if no further recursion is needed
   * @param depth the remaining number of generations to include (decrements with each recursive call)
   * @return a {@link HorseFamilyTreeDto} representing the horse’s family tree, or null if depth is 0 or horse is null
   * @throws NotFoundException if a referenced mother or father cannot be found in the persistent data store
   */
  private HorseFamilyTreeDto buildFamilyTree(Horse horse, int depth) throws NotFoundException {

    /*
    Doing the recursion here in the service layer instead of in persistence keeps the database
    logic simple and makes the recursion easier to test and maintain. Also fits better with clean
    architecture since this is business logic, not data access.
     */
    LOG.trace("Building family tree for horse {} with remaining depth {} [requestId={}]",
        horse != null ? "id " + horse.id() : "null", depth, MDC.get("r"));

    if (depth == 0 || horse == null) {
      return null;
    }

    HorseFamilyTreeDto mother = null;
    HorseFamilyTreeDto father = null;

    if (horse.motherId() != null) {
      try {
        Horse motherHorse = dao.getById(horse.motherId());
        mother = buildFamilyTree(motherHorse, depth - 1);

        LOG.debug("Added mother to family tree for horse id {} [requestId={}]: id {}", horse.id(), MDC.get("r"), horse.motherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Mother with ID {} not found for horse id {}", MDC.get("r"), horse.motherId(), horse.id(), e);

        throw new FatalException("Mother with ID " + horse.motherId() + " not found, but was referenced by horse " + horse.id(), e);
      }
    }

    if (horse.fatherId() != null) {
      try {
        Horse fatherHorse = dao.getById(horse.fatherId());
        father = buildFamilyTree(fatherHorse, depth - 1);

        LOG.debug("Added father to family tree for horse id {} [requestId={}]: id {}", horse.id(), MDC.get("r"), horse.fatherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Father with ID {} not found for horse id {}", MDC.get("r"), horse.fatherId(), horse.id(), e);

        throw new FatalException("Father with ID " + horse.fatherId() + " not found, but was referenced by horse " + horse.id(), e);
      }
    }

    return new HorseFamilyTreeDto(horse.id(), horse.name(), horse.dateOfBirth(), mother, father);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) throws ValidationException {

    LOG.trace("Entering search [requestId={}]: Searching horses with parameters {}", MDC.get("r"), searchParameters);

    validator.validateForSearch(searchParameters);

    List<Horse> horses = dao.search(searchParameters);

    LOG.debug("Found {} horses matching search parameters [requestId={}]", horses.size(), MDC.get("r"));

    if (horses.isEmpty()) {
      return Stream.empty();
    }

    var ownerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());

    Map<Long, HorseDetailOwnerDto> ownerMap;
    if (ownerIds.isEmpty()) {
      ownerMap = Collections.emptyMap();
    } else {
      try {
        ownerMap = ownerService.search(new OwnerSearchDto(searchParameters.ownerName(), ownerIds, Integer.MAX_VALUE))
            .collect(Collectors.toMap(OwnerDto::id, OwnerDto::toHorseDetailOwnerDto));

        LOG.debug("Retrieved owners for {} IDs during search [requestId={}]", ownerIds.size(), MDC.get("r"));

      } catch (ValidationException e) {
        LOG.error("Unexpected error [requestId={}]: OwnerSearchDto validation failed during horse search", MDC.get("r"), e);

        throw new FatalException("OwnerSearchDto validation failed during horse search. This should not happen. Please check mapping logic.", e);
      }
    }

    return horses.stream()
        .filter(horse -> searchParameters.ownerName() == null || (horse.ownerId() != null && ownerMap.containsKey(horse.ownerId())))
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseDetailDto create(HorseCreateDto horse, HorseImageDto image) throws ValidationException, ConflictException {

    LOG.trace("Entering create [requestId={}]: Creating horse with data {}", MDC.get("r"), horse);

    validator.validateForCreate(horse);

    var createdHorse = dao.create(horse, image);

    LOG.info("Successfully created horse with id {} [requestId={}]", createdHorse.id(), MDC.get("r"));

    HorseParentDto mother = null;
    HorseParentDto father = null;
    if (createdHorse.motherId() != null) {
      try {
        mother = dao.getParentById(createdHorse.motherId());

        LOG.debug("Retrieved mother for created horse id {} [requestId={}]: id {}", createdHorse.id(), MDC.get("r"), createdHorse.motherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Mother with ID {} not found for horse id {}, although it was validated",
            MDC.get("r"), createdHorse.motherId(), createdHorse.id(), e);

        throw new FatalException("Mother with ID " + createdHorse.motherId() + " not found, although it was validated.", e);
      }
    }
    if (createdHorse.fatherId() != null) {
      try {
        father = dao.getParentById(createdHorse.fatherId());

        LOG.debug("Retrieved father for created horse id {} [requestId={}]: id {}", createdHorse.id(), MDC.get("r"), createdHorse.fatherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Father with ID {} not found for horse id {}, although it was validated",
            MDC.get("r"), createdHorse.fatherId(), createdHorse.id(), e);

        throw new FatalException("Father with ID " + createdHorse.fatherId() + " not found, although it was validated.", e);
      }
    }

    return mapper.entityToDetailDto(createdHorse, ownerMapForSingleId(createdHorse.ownerId()), mother, father);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseDetailDto update(HorseUpdateDto horse, HorseImageDto image) throws NotFoundException, ValidationException, ConflictException {

    LOG.trace("Entering update [requestId={}]: Updating horse with id {} and data {}", MDC.get("r"), horse.id(), horse);

    validator.validateForUpdate(horse);

    var updatedHorse = dao.update(horse, image);

    LOG.info("Successfully updated horse with id {} [requestId={}]", updatedHorse.id(), MDC.get("r"));

    HorseParentDto mother = null;
    HorseParentDto father = null;
    if (updatedHorse.motherId() != null) {
      try {
        mother = dao.getParentById(updatedHorse.motherId());

        LOG.debug("Retrieved mother for updated horse id {} [requestId={}]: id {}", updatedHorse.id(), MDC.get("r"), updatedHorse.motherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Mother with ID {} not found for horse id {}, although it was validated",
            MDC.get("r"), updatedHorse.motherId(), updatedHorse.id(), e);

        throw new FatalException("Mother with ID " + updatedHorse.motherId() + " not found, although it was validated.", e);
      }
    }
    if (updatedHorse.fatherId() != null) {
      try {
        father = dao.getParentById(updatedHorse.fatherId());

        LOG.debug("Retrieved father for updated horse id {} [requestId={}]: id {}", updatedHorse.id(), MDC.get("r"), updatedHorse.fatherId());

      } catch (NotFoundException e) {
        LOG.error("Unexpected error [requestId={}]: Father with ID {} not found for horse id {}, although it was validated",
            MDC.get("r"), updatedHorse.fatherId(), updatedHorse.id(), e);

        throw new FatalException("Father with ID " + updatedHorse.fatherId() + " not found, although it was validated.", e);
      }
    }

    return mapper.entityToDetailDto(updatedHorse, ownerMapForSingleId(updatedHorse.ownerId()), mother, father);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(long id) throws NotFoundException {

    LOG.trace("Entering delete [requestId={}]: Deleting horse with id {}", MDC.get("r"), id);

    try {
      dao.delete(id);

      LOG.info("Successfully deleted horse with id {} [requestId={}]", id, MDC.get("r"));

    } catch (DataAccessException e) {
      LOG.error("Database access failed [requestId={}]: Error deleting horse with id {}", MDC.get("r"), id, e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * Creates a single-entry map of an owner ID to its detailed DTO for use in horse details.
   * Retrieves the owner from the {@link OwnerService} if an ID is provided.
   *
   * @param ownerId the ID of the owner to retrieve, or null if no owner is associated
   * @return a map containing the owner ID mapped to its {@link HorseDetailOwnerDto}, or null if ownerId is null
   * @throws FatalException if the owner with the specified ID cannot be found, wrapping a {@link NotFoundException}
   */
  private Map<Long, HorseDetailOwnerDto> ownerMapForSingleId(Long ownerId) {

    LOG.trace("Retrieving owner map for owner id {} [requestId={}]", ownerId, MDC.get("r"));

    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId).toHorseDetailOwnerDto());

    } catch (NotFoundException e) {
      LOG.error("Unexpected error [requestId={}]: Owner {} referenced by horse not found", MDC.get("r"), ownerId, e);

      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }
}
