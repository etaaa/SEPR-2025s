package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

public record HorseFamilyTreeDto(
    Long id,
    String name,
    LocalDate dateOfBirth,
    HorseFamilyTreeDto mother,
    HorseFamilyTreeDto father
) {
}
