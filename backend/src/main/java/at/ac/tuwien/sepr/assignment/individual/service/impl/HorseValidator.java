package at.ac.tuwien.sepr.assignment.individual.service.impl;


import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validator for horse-related operations, ensuring that all horse data meets the required constraints.
 */
@Component
public class HorseValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (horse.deleteImage() == null) {
      validationErrors.add("Delete image cannot be null");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
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

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

  }

}
