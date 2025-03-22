package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class OwnerValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final OwnerDao ownerDao;

  @Autowired
  public OwnerValidator(OwnerDao ownerDao) {
    this.ownerDao = ownerDao;
  }


  /**
   * Validates owner data before a create operation.
   * Ensures required fields like firstName and lastName are present and valid,
   * and checks optional fields like description if provided.
   *
   * @param owner the {@link OwnerDto} containing the owner data to validate
   * @throws ValidationException if the data is invalid (e.g., missing required fields, fields too long)
   * @throws ConflictException   if the data conflicts with existing system state (currently not implemented in this method)
   */
  public void validateForCreate(OwnerCreateDto owner) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", owner);
    List<String> validationErrors = new ArrayList<>();

    if (owner.firstName() == null || owner.firstName().isBlank()) {
      validationErrors.add("First name is required and cannot be empty");
    }
    if (owner.firstName() != null && owner.firstName().length() > 255) {
      validationErrors.add("First name too long: longer than 255 characters");
    }

    if (owner.lastName() == null || owner.lastName().isBlank()) {
      validationErrors.add("Last name is required and cannot be empty");
    }
    if (owner.lastName() != null && owner.lastName().length() > 255) {
      validationErrors.add("Last name too long: longer than 255 characters");
    }

    if (owner.description() != null) {
      if (owner.description().isBlank()) {
        validationErrors.add("Description is given but blank");
      }
      if (owner.description().length() > 4095) {
        validationErrors.add("Description too long: longer than 4095 characters");
      }
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of owner for create failed", validationErrors);
    }
  }


  /**
   * Validates search parameters for owners.
   *
   * @param searchParams the {@link OwnerSearchDto} containing the search parameters to validate
   * @throws ValidationException if the search parameters are invalid
   */
  public void validateForSearch(OwnerSearchDto searchParams) throws ValidationException {
    LOG.trace("validateForSearch({})", searchParams);
    List<String> validationErrors = new ArrayList<>();

    if (searchParams.limit() == null) {
      validationErrors.add("Limit is required for owner search");
    } else if (searchParams.limit() <= 0) {
      validationErrors.add("Limit must be a positive integer");
    }

    if (searchParams.name() != null && searchParams.name().length() > 255) {
      validationErrors.add("Search name too long: longer than 255 characters");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of owner search parameters failed", validationErrors);
    }
  }

}