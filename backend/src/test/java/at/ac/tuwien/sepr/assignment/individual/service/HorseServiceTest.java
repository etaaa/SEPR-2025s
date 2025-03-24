package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for {@link HorseService}.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
@Transactional
public class HorseServiceTest {

  @Autowired
  HorseService horseService;

  /**
   * Tests whether retrieving all stored horses returns the expected number and specific entries.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {

    List<HorseListDto> horses = horseService.getAll()
        .toList();

    assertThat(horses.size()).isGreaterThanOrEqualTo(10); // TODO: Adapt to exact number of test data entries

    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.FEMALE));
  }

  /**
   * Positive test: Creates a new horse and verifies the returned details.
   *
   * @throws ValidationException if the horse data is invalid (unexpected in this test)
   * @throws ConflictException   if the creation conflicts with existing data (unexpected in this test)
   */
  @Test
  public void createHorseSuccessfully() throws ValidationException, ConflictException {

    HorseCreateDto createDto = new HorseCreateDto(
        "New Horse",
        "A new test horse",
        LocalDate.of(2023, 1, 1),
        Sex.FEMALE,
        -1L,
        -3L, // Wendys Mother
        -4L  // Wendys Father
    );
    HorseImageDto imageDto = new HorseImageDto(
        new byte[] {1, 2, 3},
        "image/png"
    );

    HorseDetailDto created = horseService.create(createDto, imageDto);

    assertThat(created).isNotNull();
    assertThat(created.id()).isPositive();
    assertThat(created.name()).isEqualTo("New Horse");
    assertThat(created.description()).isEqualTo("A new test horse");
    assertThat(created.dateOfBirth()).isEqualTo(LocalDate.of(2023, 1, 1));
    assertThat(created.sex()).isEqualTo(Sex.FEMALE);
    assertThat(created.owner().firstName()).isEqualTo("Wendy");
    assertThat(created.mother().id()).isEqualTo(-3L);
    assertThat(created.mother().name()).isEqualTo("Wendys Mother");
    assertThat(created.father().id()).isEqualTo(-4L);
    assertThat(created.father().name()).isEqualTo("Wendys Father");
    assertThat(created.imageUrl()).isEqualTo("/horses/" + created.id() + "/image");
  }

  /**
   * Positive test: Retrieves detailed information for an existing horse.
   *
   * @throws NotFoundException if the horse with ID -6 does not exist (unexpected in test data)
   */
  @Test
  public void getByIdReturnsCorrectDetails() throws NotFoundException {

    HorseDetailDto horse = horseService.getById(-6L); // Wendy

    assertThat(horse).isNotNull();
    assertThat(horse.id()).isEqualTo(-6L);
    assertThat(horse.name()).isEqualTo("Wendy");
    assertThat(horse.description()).isEqualTo("The new one!");
    assertThat(horse.dateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
    assertThat(horse.sex()).isEqualTo(Sex.FEMALE);
    assertThat(horse.owner().firstName()).isEqualTo("Wendy");
    assertThat(horse.mother().id()).isEqualTo(-3L);
    assertThat(horse.mother().name()).isEqualTo("Wendys Mother");
    assertThat(horse.father().id()).isEqualTo(-4L);
    assertThat(horse.father().name()).isEqualTo("Wendys Father");
    assertThat(horse.imageUrl()).isNull();
  }

  /**
   * Positive test: Searches for horses with specific criteria and verifies results.
   *
   * @throws ValidationException if the search parameters are invalid (unexpected in this test)
   */
  @Test
  public void searchHorsesSuccessfully() throws ValidationException {

    HorseSearchDto searchDto = new HorseSearchDto(
        "Wendy",
        null,
        LocalDate.of(2025, 1, 1),
        null,
        null,
        null,
        10
    );

    List<HorseListDto> results = horseService.search(searchDto).toList();

    assertThat(results).isNotEmpty();
    assertThat(results.size()).isLessThanOrEqualTo(10);
    assertThat(results)
        .extracting(HorseListDto::name)
        .contains("Wendy", "Wendys Mother", "Wendys Grandmother");
    assertThat(results)
        .extracting(HorseListDto::dateOfBirth)
        .allMatch(date -> date.isBefore(LocalDate.of(2025, 1, 1)));
  }

  /**
   * Negative test: Attempts to retrieve a non-existent horse and expects NotFoundException.
   */
  @Test
  public void getByIdNonExistentThrowsNotFoundException() {

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> horseService.getById(999L)
    );
    assertThat(exception.getMessage()).contains("No horse with ID 999 found");
  }

  /**
   * Negative test: Attempts to create a horse with invalid data and expects ValidationException.
   *
   * @throws ConflictException if the creation conflicts with existing data (not expected here)
   */
  @Test
  public void createHorseWithInvalidDataThrowsValidationException() throws ConflictException {

    HorseCreateDto invalidDto = new HorseCreateDto(
        "", // Invalid name
        "Valid description",
        LocalDate.of(2025, 3, 25), // Invalid date
        null, // Invalid sex
        -1L,
        -3L,
        -4L
    );

    ValidationException exception = assertThrows(ValidationException.class,
        () -> horseService.create(invalidDto, null)
    );
    assertThat(exception.getMessage()).contains(
        "Horse name is required and cannot be empty",
        "Horse birth date cannot be in the future",
        "Sex is required"
    );
  }

  /**
   * Negative test: Attempts to create a horse with a non-existent owner and expects ValidationException.
   *
   * @throws ConflictException if the creation conflicts with existing data (not expected here)
   */
  @Test
  public void createHorseWithNonExistentOwnerThrowsValidationException() throws ConflictException {

    HorseCreateDto invalidDto = new HorseCreateDto(
        "Valid Horse",
        "Valid description",
        LocalDate.of(2023, 1, 1),
        Sex.MALE,
        999L, // Invalid owner
        -3L,
        -4L
    );

    ValidationException exception = assertThrows(ValidationException.class,
        () -> horseService.create(invalidDto, null)
    );
    assertThat(exception.getMessage()).contains("Owner with ID 999 does not exist");
  }
}