package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO to bundle the query parameters used in searching horses.
 * Each field can be null, in which case this field is not filtered by.
 */
public record HorseSearchDto(
    String name,
    String description,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateOfBirth,
    Sex sex,
    String ownerName,
    Long excludeId,
    Integer limit
) {

  /**
   * Checks if all search parameters are empty.
   *
   * @return true if all search parameters are null, false otherwise
   */
  public boolean isEmpty() {
    return name == null
        && description == null
        && dateOfBirth == null
        && sex == null
        && ownerName == null
        && excludeId == null
        && limit == null;
  }
}