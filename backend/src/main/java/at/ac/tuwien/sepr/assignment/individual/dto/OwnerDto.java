package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * Represents a Data Transfer Object (DTO) for owner details.
 * This record encapsulates the essential information about an owner.
 */
public record OwnerDto(
    long id,
    String firstName,
    String lastName,
    String description
) {

  /**
   * Converts the owner DTO to a HorseDetailOwnerDto.
   * Extracts first and last name for use in horse detail contexts.
   *
   * @return A HorseDetailOwnerDto containing the owner's first and last name
   */
  public HorseDetailOwnerDto toHorseDetailOwnerDto() {
    return new HorseDetailOwnerDto(firstName, lastName);
  }

}
