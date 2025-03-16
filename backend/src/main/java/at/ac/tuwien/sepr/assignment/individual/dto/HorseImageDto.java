package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * Represents a Data Transfer Object (DTO) for holding horse image data.
 * This record encapsulates the binary image data along with its MIME type.
 *
 * @param image    the binary image data of the horse
 * @param mimeType the MIME type of the image (e.g., "image/jpeg", "image/png")
 */
public record HorseImageDto(
    byte[] image,
    String mimeType
) {
}
