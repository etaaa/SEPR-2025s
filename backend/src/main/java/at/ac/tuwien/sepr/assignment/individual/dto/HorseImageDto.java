package at.ac.tuwien.sepr.assignment.individual.dto;

public record HorseImageDto(
    byte[] image,
    String mimeType
) {
}
