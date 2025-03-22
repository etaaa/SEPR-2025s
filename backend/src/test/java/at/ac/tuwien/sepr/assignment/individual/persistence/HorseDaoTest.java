package at.ac.tuwien.sepr.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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

/**
 * Integration test for {@link HorseDao}, ensuring database operations function correctly.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile to load test data
@SpringBootTest
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;

  /**
   * Tests that retrieving all stored horses returns at least one entry
   * and verifies that a specific horse exists in the test dataset.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.search(new HorseSearchDto(null, null, null, null, null, null, null));
    assertThat(horses.size()).isGreaterThanOrEqualTo(3); // TODO adapt to exact number of elements in test data later
    assertThat(horses)
        .extracting(Horse::id, Horse::name)
        .contains(tuple(-1L, "Wendys Grandfather"));
  }

  /**
   * Tests successful retrieval of an existing horse by ID.
   * Verifies that all fields contain the expected data.
   */
  @Test
  public void getByIdWithExistingIdSucceeds() throws NotFoundException {
    Horse horse = horseDao.getById(-1L);

    assertThat(horse).isNotNull();
    assertThat(horse.id()).isEqualTo(-1L);
    assertThat(horse.sex()).isEqualTo(Sex.MALE);
    assertThat(horse.name()).isNotNull();
    assertThat(horse.dateOfBirth()).isNotNull();
  }

  /**
   * Tests successful creation of a new horse in the persistent store.
   * Verifies that the created horse is returned with correct details.
   */
  @Test
  public void createHorseWithValidDataSucceeds() {
    HorseCreateDto createDto = new HorseCreateDto(
        "Test Horse",
        "Test Description",
        LocalDate.of(2020, 1, 1),
        Sex.MALE,
        null,
        null,
        null
    );
    HorseImageDto imageDto = new HorseImageDto(
        new byte[] {1, 2, 3},
        "image/jpeg"
    );

    Horse createdHorse = horseDao.create(createDto, imageDto);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.id()).isNotNull().isPositive();
    assertThat(createdHorse.name()).isEqualTo("Test Horse");
    assertThat(createdHorse.description()).isEqualTo("Test Description");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(LocalDate.of(2020, 1, 1));
    assertThat(createdHorse.sex()).isEqualTo(Sex.MALE);
    assertThat(createdHorse.ownerId()).isNull();
    assertThat(createdHorse.imageUrl()).isEqualTo("/horses/" + createdHorse.id() + "/image");
  }

  /**
   * Tests retrieval of a horse with a non-existing ID.
   * Verifies that a NotFoundException is thrown.
   */
  @Test
  public void getByIdWithNonExistingIdFails() {
    assertThrows(NotFoundException.class, () -> {
      horseDao.getById(999L);
    });
  }

  /**
   * Tests update of a non-existing horse.
   * Verifies that a NotFoundException is thrown.
   */
  @Test
  public void updateHorseWithNonExistingIdFails() {
    HorseUpdateDto updateDto = new HorseUpdateDto(
        999L,
        "Updated Horse",
        "Updated Description",
        LocalDate.of(2020, 1, 1),
        Sex.FEMALE,
        null,
        null,
        null,
        false
    );
    HorseImageDto imageDto = new HorseImageDto(
        new byte[] {4, 5, 6},
        "image/png"
    );

    assertThrows(NotFoundException.class, () -> {
      horseDao.update(updateDto, imageDto);
    });
  }

}
