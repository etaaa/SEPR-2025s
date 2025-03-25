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
  public HorseServiceImpl(
      HorseDao dao,
      HorseMapper mapper,
      HorseValidator validator,
      OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }


  @Override
  public HorseDetailDto getById(long id)
      throws NotFoundException {

    /*
    In this method we won't have to validate the ID (check if there
    exists a horse with the given ID), as that would be useless. We
    would make the same query twice in that case (see
    https://tuwel.tuwien.ac.at/mod/forum/discuss.php?d=478592).
     */
    LOG.trace("details({})", id);

    Horse horse = dao.getById(id);

    HorseParentDto mother = null;
    HorseParentDto father = null;
    if (horse.motherId() != null) {
      try {
        mother = dao.getParentById(horse.motherId());

      } catch (NotFoundException e) {
        throw new FatalException("Mother with ID " + horse.motherId() + " not found, although it was validated.", e);
      }
    }
    if (horse.fatherId() != null) {
      try {
        father = dao.getParentById(horse.fatherId());

      } catch (NotFoundException e) {
        throw new FatalException("Father with ID " + horse.motherId() + " not found, although it was validated.", e);
      }
    }

    return mapper.entityToDetailDto(
        horse,
        ownerMapForSingleId(horse.ownerId()),
        mother,
        father);
  }


  @Override
  public HorseImageDto getImageById(long id)
      throws NotFoundException {

    /*
     The recursive family tree logic is implemented here in the service
     layer to keep the persistence layer focused on simple data access.
     This follows clean architecture principles by separating business
     logic from database queries, improves testability, and avoids
     database-specific SQL features.
     */
    LOG.trace("getImageById({})", id);
    return dao.getImageById(id);
  }


  @Override
  public Stream<HorseListDto> getAll() {

    LOG.trace("allHorses()");

    var horses = dao.getAll();

    var ownerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());

    Map<Long, HorseDetailOwnerDto> ownerMap;

    try {
      ownerMap = ownerService.getAllById(ownerIds);

    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }

    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }


  @Override
  public HorseFamilyTreeDto getFamilyTree(long id, int depth)
      throws NotFoundException, ValidationException {

    LOG.trace("getFamilyTree({}, {})", id, depth);

    validator.validateGenerations(depth);

    Horse horse = dao.getById(id);

    return buildFamilyTree(horse, depth);
  }


  private HorseFamilyTreeDto buildFamilyTree(Horse horse, int depth)
      throws NotFoundException {

    if (depth == 0 || horse == null) {
      return null;
    }

    HorseFamilyTreeDto mother = null;
    HorseFamilyTreeDto father = null;

    if (horse.motherId() != null) {
      try {
        Horse motherHorse = dao.getById(horse.motherId());
        mother = buildFamilyTree(motherHorse, depth - 1);
      } catch (NotFoundException e) {
        throw new FatalException("Mother with ID " + horse.motherId() + " not found, but was referenced by horse " + horse.id(), e);
      }
    }

    if (horse.fatherId() != null) {
      try {
        Horse fatherHorse = dao.getById(horse.fatherId());
        father = buildFamilyTree(fatherHorse, depth - 1);
      } catch (NotFoundException e) {
        throw new FatalException("Father with ID " + horse.fatherId() + " not found, but was referenced by horse " + horse.id(), e);
      }
    }

    return new HorseFamilyTreeDto(
        horse.id(),
        horse.name(),
        horse.dateOfBirth(),
        mother,
        father
    );
  }


  @Override
  public Stream<HorseListDto> search(HorseSearchDto searchParameters)
      throws ValidationException {

    LOG.trace("search({})", searchParameters);

    validator.validateForSearch(searchParameters);

    List<Horse> horses = dao.search(searchParameters);

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
        /*
        Convert the stream of OwnerDto into a map, where the key is the ownerId and the value the
        mapped HorseDetailOwnerDto
        */
        ownerMap = ownerService.search(new OwnerSearchDto(searchParameters.ownerName(), ownerIds, Integer.MAX_VALUE))
            .collect(Collectors.toMap(OwnerDto::id, OwnerDto::toHorseDetailOwnerDto));

      } catch (ValidationException e) {
        throw new FatalException("OwnerSearchDto validation failed during horse search. This should not happen. Please check mapping logic.", e);
      }
    }

    return horses.stream()
        .filter(horse -> searchParameters.ownerName() == null || (horse.ownerId() != null && ownerMap.containsKey(horse.ownerId())))
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }


  @Override
  public HorseDetailDto create(HorseCreateDto horse, HorseImageDto image)
      throws ValidationException, ConflictException {
    LOG.trace("create({})", horse);

    validator.validateForCreate(horse);

    var createdHorse = dao.create(horse, image);

    HorseParentDto mother = null;
    HorseParentDto father = null;
    if (createdHorse.motherId() != null) {
      try {
        mother = dao.getParentById(createdHorse.motherId());
      } catch (NotFoundException e) {
        throw new FatalException("Mother with ID " + createdHorse.motherId() + " not found, although it was validated.", e);
      }
    }
    if (createdHorse.fatherId() != null) {
      try {
        father = dao.getParentById(createdHorse.fatherId());
      } catch (NotFoundException e) {
        throw new FatalException("Father with ID " + createdHorse.motherId() + " not found, although it was validated.", e);
      }
    }

    return mapper.entityToDetailDto(
        createdHorse,
        ownerMapForSingleId(createdHorse.ownerId()),
        mother,
        father);
  }


  @Override
  public HorseDetailDto update(HorseUpdateDto horse, HorseImageDto image)
      throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);

    validator.validateForUpdate(horse);

    var updatedHorse = dao.update(horse, image);

    HorseParentDto mother = null;
    HorseParentDto father = null;
    if (updatedHorse.motherId() != null) {
      try {
        mother = dao.getParentById(updatedHorse.motherId());
      } catch (NotFoundException e) {
        throw new FatalException("Mother with ID " + updatedHorse.motherId() + " not found, although it was validated.", e);
      }
    }
    if (updatedHorse.fatherId() != null) {
      try {
        father = dao.getParentById(updatedHorse.fatherId());
      } catch (NotFoundException e) {
        throw new FatalException("Father with ID " + updatedHorse.fatherId() + " not found, although it was validated.", e);
      }
    }

    return mapper.entityToDetailDto(
        updatedHorse,
        ownerMapForSingleId(updatedHorse.ownerId()),
        mother,
        father);

  }


  @Override
  public void delete(long id)
      throws NotFoundException {

    LOG.trace("delete({})", id);

    try {
      dao.delete(id);

    } catch (DataAccessException e) {
      throw new PersistenceException("Error accessing database", e);
    }
  }


  private Map<Long, HorseDetailOwnerDto> ownerMapForSingleId(Long ownerId) {

    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId).toHorseDetailOwnerDto());
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

}
