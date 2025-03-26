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
    Long ownerId,
    Long motherId,
    Long fatherId,
    Boolean deleteImage
) {

  /**
   * Converts the REST DTO to a full update DTO by adding the provided ID.
   *
   * @param id The ID to be associated with the horse update
   * @return A new HorseUpdateDto with the provided ID and other details
   */
  public HorseUpdateDto toUpdateDtoWithId(Long id) {
    return new HorseUpdateDto(id, name, description, dateOfBirth, sex, ownerId, motherId, fatherId, deleteImage);
  }

}
