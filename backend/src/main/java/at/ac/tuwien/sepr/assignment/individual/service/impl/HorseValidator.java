package at.ac.tuwien.sepr.assignment.individual.service.impl;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator for horse-related operations, ensuring that all horse data meets the required constraints.
 */
@Component
public class HorseValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao horseDao;
  private final OwnerService ownerService;

  @Autowired
  public HorseValidator(HorseDao horseDao, OwnerService ownerService) {
    this.horseDao = horseDao;
    this.ownerService = ownerService;
  }

  /**
   * Validates search parameters for querying horses.
   * Ensures that search parameters adhere to structural constraints, such as maximum string lengths and valid limits.
   *
   * @param searchParams the search parameters to validate
   * @throws ValidationException if any validation rule is violated (e.g., name exceeds 255 characters, limit is null or negative)
   */
  public void validateForSearch(HorseSearchDto searchParams) throws ValidationException {

    /*
    Note: This is for structural validation, therefore we should not check if the ownerIds actually
    exists. It's totally valid for the search result to contain fewer entries then requested (if a
    few owners don't exist).
     */
    LOG.trace("Entering validateForSearch [requestId={}]: Validating search parameters {}", MDC.get("r"), searchParams);

    List<String> validationErrors = new ArrayList<>();

    if (searchParams.name() != null && searchParams.name().length() > 255) {
      validationErrors.add("Search name too long: must be 255 characters or fewer");
    }

    if (searchParams.description() != null && searchParams.description().length() > 4095) {
      validationErrors.add("Search description too long: must be 4095 characters or fewer");
    }

    if (searchParams.ownerName() != null && searchParams.ownerName().length() > 255) {
      validationErrors.add("Owner name too long: must be 255 characters or fewer");
    }

    if (searchParams.limit() == null) {
      validationErrors.add("Search limit is required");
    } else if (searchParams.limit() <= 0) {
      validationErrors.add("Search limit must be greater or equal to 1");
    }

    if (!validationErrors.isEmpty()) {
      LOG.warn("Validation of horse search parameters failed [requestId={}]: {}", MDC.get("r"), validationErrors);
      throw new ValidationException("Validation of horse search parameters failed", validationErrors);
    }

    LOG.debug("Successfully validated search parameters [requestId={}]: {}", MDC.get("r"), searchParams);
  }

  /**
   * Validates the number of generations for pedigree-related queries.
   * Ensures the number of generations is within acceptable bounds (1 to 10).
   *
   * @param generations the number of generations to validate
   * @throws ValidationException if generations is less than 1 or exceeds 10
   */
  public void validateGenerations(int generations) throws ValidationException {

    LOG.trace("Entering validateGenerations [requestId={}]: Validating generations {}", MDC.get("r"), generations);

    List<String> validationErrors = new ArrayList<>();

    if (generations < 1) {
      validationErrors.add("Generations must be at minimum 1");
    }
    if (generations > 10) {
      validationErrors.add("Generations must not exceed 10");
    }

    if (!validationErrors.isEmpty()) {
      LOG.warn("Validation of generations parameter failed [requestId={}]: {}", MDC.get("r"), validationErrors);
      throw new ValidationException("Validation of generations parameter failed", validationErrors);
    }

    LOG.debug("Successfully validated generations [requestId={}]: {}", MDC.get("r"), generations);
  }

  /**
   * Validates horse data before creation.
   * Ensures required fields (e.g., name, date of birth, sex) are present and valid, and checks for conflicts with existing data.
   *
   * @param horse the data transfer object containing horse details to validate
   * @throws ValidationException if required fields are missing or invalid (e.g., empty name, future birth date)
   * @throws ConflictException   if data conflicts with existing records (e.g., mother is not female, parents not older than child)
   */
  public void validateForCreate(HorseCreateDto horse) throws ValidationException, ConflictException {

    LOG.trace("Entering validateForCreate [requestId={}]: Validating horse creation with data {}", MDC.get("r"), horse);

    List<String> validationErrors = new ArrayList<>();

    if (horse.name() == null || horse.name().isBlank()) {
      validationErrors.add("Horse name is required and cannot be empty");
    }
    if (horse.name() != null && horse.name().length() > 255) {
      validationErrors.add("Horse name too long: longer than 255 characters");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (horse.dateOfBirth() == null) {
      validationErrors.add("Horse birth date is required");
    } else {
      LocalDate today = LocalDate.now();
      if (horse.dateOfBirth().isAfter(today)) {
        validationErrors.add("Horse birth date cannot be in the future");
      }
    }

    if (horse.sex() == null) {
      validationErrors.add("Sex is required");
    }

    if (horse.ownerId() != null) {
      try {
        ownerService.getById(horse.ownerId());
      } catch (NotFoundException e) {
        validationErrors.add("Owner with ID " + horse.ownerId() + " does not exist");
      }
    }

    List<String> conflictErrors = new ArrayList<>();

    if (horse.motherId() != null) {
      try {
        Horse mother = horseDao.getById(horse.motherId());
        if (mother.sex() != Sex.FEMALE) {
          conflictErrors.add("Sex of mother has to be FEMALE");
        }
        if (!horse.dateOfBirth().isAfter(mother.dateOfBirth())) {
          conflictErrors.add("Mother has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Mother with ID " + horse.motherId() + " does not exist");
      }
    }

    if (horse.fatherId() != null) {
      try {
        Horse father = horseDao.getById(horse.fatherId());
        if (father.sex() != Sex.MALE) {
          conflictErrors.add("Sex of father has to be MALE");
        }
        if (!horse.dateOfBirth().isAfter(father.dateOfBirth())) {
          conflictErrors.add("Father has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Father with ID " + horse.fatherId() + " does not exist");
      }
    }

    if (!validationErrors.isEmpty()) {
      LOG.warn("Validation of horse for create failed [requestId={}]: {}", MDC.get("r"), validationErrors);
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      LOG.warn("Conflict detected during horse creation [requestId={}]: {}", MDC.get("r"), conflictErrors);
      throw new ConflictException("Conflict with existing data", conflictErrors);
    }

    LOG.debug("Successfully validated horse for creation [requestId={}]: {}", MDC.get("r"), horse);
  }

  /**
   * Validates horse data before updating an existing horse.
   * Ensures all fields meet constraints and checks for conflicts with existing data, such as parent-child relationships.
   *
   * @param horse the data transfer object containing updated horse details
   * @throws ValidationException if validation fails (e.g., missing name, future birth date)
   * @throws ConflictException   if conflicts are detected (e.g., changing sex of a parent, birth date after child's birth)
   */
  public void validateForUpdate(HorseUpdateDto horse) throws ValidationException, ConflictException {

    LOG.trace("Entering validateForUpdate [requestId={}]: Validating horse update with data {}", MDC.get("r"), horse);

    List<String> validationErrors = new ArrayList<>();
    List<String> conflictErrors = new ArrayList<>();

    if (horse.id() != null) {
      try {
        Horse existingHorse = horseDao.getById(horse.id());
        List<Horse> children = horseDao.getChildrenByParentId(horse.id());

        if (!children.isEmpty()) {
          if (horse.sex() != null && !horse.sex().equals(existingHorse.sex())) {
            conflictErrors.add("Cannot change sex of a horse that has children");
          }

          if (horse.dateOfBirth() != null && !horse.dateOfBirth().equals(existingHorse.dateOfBirth())) {
            for (Horse child : children) {
              if (!horse.dateOfBirth().isBefore(child.dateOfBirth())) {
                conflictErrors.add("Cannot change date of birth to be after a child's birth date");
                break;
              }
            }
          }
        }
      } catch (NotFoundException e) {
        validationErrors.add("Horse with ID " + horse.id() + " does not exist");
      }
    }

    if (horse.name() == null || horse.name().isBlank()) {
      validationErrors.add("Horse name is required and cannot be empty");
    }
    if (horse.name() != null && horse.name().length() > 255) {
      validationErrors.add("Horse name too long: longer than 255 characters");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (horse.dateOfBirth() == null) {
      validationErrors.add("Horse birth date is required");
    } else {
      LocalDate today = LocalDate.now();
      if (horse.dateOfBirth().isAfter(today)) {
        validationErrors.add("Horse birth date cannot be in the future");
      }
    }

    if (horse.sex() == null) {
      validationErrors.add("Sex is required");
    }

    if (horse.ownerId() != null) {
      try {
        ownerService.getById(horse.ownerId());
      } catch (NotFoundException e) {
        validationErrors.add("Owner with ID " + horse.ownerId() + " does not exist");
      }
    }

    if (horse.motherId() != null) {
      try {
        Horse mother = horseDao.getById(horse.motherId());
        if (mother.sex() != Sex.FEMALE) {
          conflictErrors.add("Sex of mother has to be FEMALE");
        }
        if (!horse.dateOfBirth().isAfter(mother.dateOfBirth())) {
          conflictErrors.add("Mother has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Mother with ID " + horse.motherId() + " does not exist");
      }
    }

    if (horse.fatherId() != null) {
      try {
        Horse father = horseDao.getById(horse.fatherId());
        if (father.sex() != Sex.MALE) {
          conflictErrors.add("Sex of father has to be MALE");
        }
        if (!horse.dateOfBirth().isAfter(father.dateOfBirth())) {
          conflictErrors.add("Father has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Father with ID " + horse.fatherId() + " does not exist");
      }
    }

    if (horse.deleteImage() == null) {
      validationErrors.add("Delete image cannot be null");
    }

    if (!validationErrors.isEmpty()) {
      LOG.warn("Validation of horse for update failed [requestId={}]: {}", MDC.get("r"), validationErrors);
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      LOG.warn("Conflict detected during horse update [requestId={}]: {}", MDC.get("r"), conflictErrors);
      throw new ConflictException("Conflict with existing data", conflictErrors);
    }

    LOG.debug("Successfully validated horse for update [requestId={}]: {}", MDC.get("r"), horse);
  }
}
