package at.ac.tuwien.sepr.assignment.individual.service.impl;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
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
  We use services here instead of DAOs, as directly accessing the DAOs
  would violate the layered architecture. Another advantage is that when
  verifying an ID, for example, we can add extra checks to the getByID()
  method in the service layer—checks that wouldn’t be applied if we called
  the method directly in the persistence layer.
   */
  private final HorseDao horseDao;
  private final OwnerDao ownerDao;

  @Autowired
  public HorseValidator(HorseDao horseDao, OwnerDao ownerDao) {
    this.horseDao = horseDao;
    this.ownerDao = ownerDao;
  }

  public void validateGenerations(int generations) throws ValidationException {
    LOG.trace("validateGenerations({})", generations);
    List<String> validationErrors = new ArrayList<>();

    if (generations < 0) {
      validationErrors.add("Generations must be a non-negative integer");
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
   * @throws ConflictException   if the data conflicts with existing system state (currently not implemented in this method)
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
        // TODO: conflictexception instead?
        validationErrors.add("Owner with ID " + horse.ownerId() + " does not exist");
      }
    }

    if (horse.motherId() != null) {
      try {
        Horse mother = horseDao.getById(horse.motherId());
        if (mother.sex() != Sex.FEMALE) {
          validationErrors.add("Sex of mother has to be FEMALE");
        }
        if (!horse.dateOfBirth().isAfter(mother.dateOfBirth())) {
          validationErrors.add("Mother has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Mother with ID " + horse.motherId() + " does not exist");
      }
    }

    if (horse.fatherId() != null) {
      try {
        Horse father = horseDao.getById(horse.fatherId());
        if (father.sex() != Sex.MALE) {
          validationErrors.add("Sex of father has to be MALE");
        }
        if (!horse.dateOfBirth().isAfter(father.dateOfBirth())) {
          validationErrors.add("Father has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Father with ID " + horse.motherId() + " does not exist");
      }
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for create failed", validationErrors);
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

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    } else {
      try {
        horseDao.getById(horse.id());
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
          validationErrors.add("Sex of mother has to be FEMALE");
        }
        if (!horse.dateOfBirth().isAfter(mother.dateOfBirth())) {
          validationErrors.add("Mother has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Mother with ID " + horse.motherId() + " does not exist");
      }
    }

    if (horse.fatherId() != null) {
      try {
        Horse father = horseDao.getById(horse.fatherId());
        if (father.sex() != Sex.MALE) {
          validationErrors.add("Sex of father has to be MALE");
        }
        if (!horse.dateOfBirth().isAfter(father.dateOfBirth())) {
          validationErrors.add("Father has to be older than her child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Father with ID " + horse.motherId() + " does not exist");
      }
    }

    if (horse.deleteImage() == null) {
      validationErrors.add("Delete image cannot be null");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

  }

}
