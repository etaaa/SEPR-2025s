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
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator for horse-related operations, ensuring that all horse data meets the required constraints.
 */
@Component
public class HorseValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /*
  Note: We use services here instead of DAOs, as directly accessing the DAOs
  would violate the layered architecture. Another advantage is that when
  verifying an ID, for example, we can add extra checks to the getByID()
  method in the service layer—checks that wouldn't be applied if we called
  the method directly in the persistence layer.
   */
  private final HorseDao horseDao;
  private final OwnerDao ownerDao;

  @Autowired
  public HorseValidator(HorseDao horseDao, OwnerDao ownerDao) {
    this.horseDao = horseDao;
    this.ownerDao = ownerDao;
  }


  public void validateForSearch(HorseSearchDto searchParams) throws ValidationException {

    /*
    Note: This is for structural validation, therefore we should not check if the ownerIds actually
    exists. It's totally valid for the search result to contain fewer entries then requested (if a
    few owners don't exist).
     */
    LOG.trace("validateForSearch({})", searchParams);

    List<String> validationErrors = new ArrayList<>();

    /*
    Note: This limits only the search input, not the underlying full name, which can be longer.
     */
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
      throw new ValidationException("Validation of horse search parameters failed", validationErrors);
    }
  }


  public void validateGenerations(int generations) throws ValidationException {

    LOG.trace("validateGenerations({})", generations);

    List<String> validationErrors = new ArrayList<>();

    if (generations < 1) {
      validationErrors.add("Generations must be at minimum 1");
    }
    if (generations > 25) {
      validationErrors.add("Generations must not exceed 25");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of generations parameter failed", validationErrors);
    }
  }

  /**
   * Validates horse data before a create operation.
   * Ensures required fields are present and valid, such as name, date of birth, and sex.
   *
   * @param horse the {@link HorseCreateDto} containing the horse data to validate
   * @throws ValidationException if the data is invalid (e.g., missing name, future birth date, missing sex)
   * @throws ConflictException   if the data conflicts with existing system state
   */
  public void validateForCreate(HorseCreateDto horse) throws ValidationException, ConflictException {

    LOG.trace("validateForCreate({})", horse);

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
        ownerDao.getById(horse.ownerId());
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
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Conflict with existing data", conflictErrors);
    }
  }

  /**
   * Validates a horse before updating, ensuring all fields meet constraints and checking for conflicts.
   *
   * @param horse the {@link HorseUpdateDto} to validate
   * @throws ValidationException if validation fails
   * @throws ConflictException   if conflicts with existing data are detected
   */
  public void validateForUpdate(HorseUpdateDto horse) throws ValidationException, ConflictException {

    LOG.trace("validateForUpdate({})", horse);

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
        ownerDao.getById(horse.ownerId());
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
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Conflict with existing data", conflictErrors);
    }
  }
}
