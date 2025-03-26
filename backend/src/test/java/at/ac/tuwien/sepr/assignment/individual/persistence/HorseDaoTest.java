package at.ac.tuwien.sepr.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;

import java.time.LocalDate;
import java.util.List;

import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test for {@link HorseDao}, ensuring database operations function correctly.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile to load test data
@SpringBootTest
@Transactional
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;

  /**
   * Positive test: Verifies that retrieving all horses returns the expected number and a specific horse from test data.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {

    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isGreaterThanOrEqualTo(10);
    assertThat(horses)
        .extracting(Horse::id, Horse::name)
        .contains(tuple(-1L, "Wendys Grandmother"));
  }

  /**
   * Positive test: Creates a new horse with valid data and image, and verifies it was persisted correctly.
   */
  @Test
  public void createHorseSuccessfully() {

    HorseCreateDto createDto = new HorseCreateDto(
        "Test Horse",
        "A test horse",
        LocalDate.of(2023, 1, 1),
        Sex.MALE,
        -1L,
        -3L, // Wendys Mother
        -4L  // Wendys Father
    );
    HorseImageDto imageDto = new HorseImageDto(
        new byte[] {1, 2, 3},
        "image/png"
    );

    Horse created = horseDao.create(createDto, imageDto);

    assertThat(created).isNotNull();
    assertAll(
        () -> assertThat(created.id()).isPositive(),
        () -> assertThat(created.name()).isEqualTo("Test Horse"),
        () -> assertThat(created.description()).isEqualTo("A test horse"),
        () -> assertThat(created.dateOfBirth()).isEqualTo(LocalDate.of(2023, 1, 1)),
        () -> assertThat(created.sex()).isEqualTo(Sex.MALE),
        () -> assertThat(created.ownerId()).isEqualTo(-1L),
        () -> assertThat(created.motherId()).isEqualTo(-3L),
        () -> assertThat(created.fatherId()).isEqualTo(-4L),
        () -> assertThat(created.imageUrl()).isEqualTo("/horses/" + created.id() + "/image")
    );
  }

  /**
   * Positive test: Retrieves an existing horse by ID and verifies all fields match expected test data.
   *
   * @throws NotFoundException if the horse with ID -6 does not exist (not expected with test data)
   */
  @Test
  public void getByIdReturnsCorrectHorse() throws NotFoundException {

    Horse horse = horseDao.getById(-6L); // Wendy

    assertThat(horse).isNotNull();
    assertAll(
        () -> assertThat(horse.id()).isEqualTo(-6L),
        () -> assertThat(horse.name()).isEqualTo("Wendy"),
        () -> assertThat(horse.description()).isEqualTo("The new one!"),
        () -> assertThat(horse.dateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1)),
        () -> assertThat(horse.sex()).isEqualTo(Sex.FEMALE),
        () -> assertThat(horse.ownerId()).isEqualTo(-1L),
        () -> assertThat(horse.motherId()).isEqualTo(-3L),
        () -> assertThat(horse.fatherId()).isEqualTo(-4L),
        () -> assertThat(horse.imageUrl()).isNull()
    );
  }

  /**
   * Negative test: Attempts to retrieve a horse with a non-existent ID and verifies a NotFoundException is thrown.
   */
  @Test
  public void getByIdNonExistentThrowsNotFoundException() {

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> horseDao.getById(999L)
    );
    assertThat(exception.getMessage()).contains("No horse with ID 999 found");
  }

  /**
   * Negative test: Attempts to delete a horse with a non-existent ID and verifies a NotFoundException is thrown.
   */
  @Test
  public void deleteNonExistentHorseThrowsNotFoundException() {

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> horseDao.delete(999L)
    );
    assertThat(exception.getMessage()).contains("No horse with ID 999 found for deletion");
  }

  /**
   * Positive test: Updates an existing horse with new data and image, and verifies the changes were persisted.
   *
   * @throws NotFoundException if the horse with ID -7 does not exist (not expected with test data)
   */
  @Test
  public void updateHorseSuccessfully() throws NotFoundException {

    HorseUpdateDto updateDto = new HorseUpdateDto(
        -7L, // Wendys Husband
        "Updated Husband",
        "The updated strong one!",
        LocalDate.of(2000, 1, 1),
        Sex.MALE,
        -2L,
        null,
        null,
        false
    );
    HorseImageDto imageDto = new HorseImageDto(
        new byte[] {4, 5, 6},
        "image/jpeg"
    );

    Horse updated = horseDao.update(updateDto, imageDto);

    assertThat(updated).isNotNull();
    assertAll(
        () -> assertThat(updated.id()).isEqualTo(-7L),
        () -> assertThat(updated.name()).isEqualTo("Updated Husband"),
        () -> assertThat(updated.description()).isEqualTo("The updated strong one!"),
        () -> assertThat(updated.dateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1)),
        () -> assertThat(updated.sex()).isEqualTo(Sex.MALE),
        () -> assertThat(updated.ownerId()).isEqualTo(-2L),
        () -> assertThat(updated.motherId()).isNull(),
        () -> assertThat(updated.fatherId()).isNull(),
        () -> assertThat(updated.imageUrl()).isEqualTo("/horses/-7/image")
    );
  }

  /**
   * Positive test: Searches for horses with criteria that should yield no results and verifies an empty list is returned.
   */
  @Test
  public void searchWithImpossibleCriteriaReturnsEmptyList() {

    HorseSearchDto searchDto = new HorseSearchDto(
        "NonExistentHorse123",
        null,
        null,
        Sex.FEMALE,
        null,
        null,
        5
    );

    List<Horse> results = horseDao.search(searchDto);

    assertAll(
        () -> assertThat(results).isNotNull(),
        () -> assertThat(results).isEmpty()
    );
  }
}