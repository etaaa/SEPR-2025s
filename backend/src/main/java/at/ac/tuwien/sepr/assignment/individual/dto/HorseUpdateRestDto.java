package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;

/**
 * REST-DTO for updating horses.
 * Contains the same fields as the normal update DTO, without the ID (which should come from the request URL instead)
 */
public record HorseUpdateRestDto(
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    Long ownerId
) {

  public HorseUpdateDto toUpdateDtoWithId(Long id) {
    return new HorseUpdateDto(id, name, description, dateOfBirth, sex, ownerId);
  }

}
