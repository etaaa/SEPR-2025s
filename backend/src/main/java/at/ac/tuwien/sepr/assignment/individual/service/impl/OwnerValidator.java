package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Validator for owner-related operations, ensuring that all owner data meets the required constraints.
 */
@Component
public class OwnerValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Validates search parameters for querying owners.
   * Ensures structural constraints are met, such as maximum string lengths and valid limits.
   *
   * @param searchParams the search parameters to validate
   * @throws ValidationException if validation fails (e.g., name exceeds 255 characters, limit is null or negative)
   */
  public void validateForSearch(OwnerSearchDto searchParams) throws ValidationException {

    /*
    This is for structural validation, therefore we should not check if the ownerIds actually
    exists. It's totally valid for the search result to contain fewer entries then requested (if
    a few owner don't exist).
     */
    LOG.trace("Entering validateForSearch [requestId={}]: Validating search parameters {}", MDC.get("r"), searchParams);

    List<String> validationErrors = new ArrayList<>();

    if (searchParams.name() != null && searchParams.name().length() > 255) {
      validationErrors.add("Search name too long: longer than 255 characters");
    }

    if (searchParams.limit() == null) {
      validationErrors.add("Search limit is required");
    } else if (searchParams.limit() <= 0) {
      validationErrors.add("Search limit must be greater or equal to 1");
    }

    if (!validationErrors.isEmpty()) {
      LOG.warn("Validation of owner search parameters failed [requestId={}]: {}", MDC.get("r"), validationErrors);
      throw new ValidationException("Validation of owner search parameters failed", validationErrors);
    }

    LOG.debug("Successfully validated search parameters [requestId={}]: {}", MDC.get("r"), searchParams);
  }

  /**
   * Validates owner data before creation.
   * Ensures required fields (first name, last name) are present and valid, and checks optional fields (description) if provided.
   *
   * @param owner the data transfer object containing owner details to validate
   * @throws ValidationException if validation fails (e.g., missing first name, description too long)
   */
  public void validateForCreate(OwnerCreateDto owner) throws ValidationException {

    LOG.trace("Entering validateForCreate [requestId={}]: Validating owner creation with data {}", MDC.get("r"), owner);

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
      LOG.warn("Validation of owner for create failed [requestId={}]: {}", MDC.get("r"), validationErrors);
      throw new ValidationException("Validation of owner for create failed", validationErrors);
    }

    LOG.debug("Successfully validated owner for creation [requestId={}]: {}", MDC.get("r"), owner);
  }
}