package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for {@link HorseService}.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
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

    assertThat(horses.size()).isGreaterThanOrEqualTo(4);

    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.MALE));
  }

  /**
   * Tests successful creation of a new horse with valid data.
   * Verifies that the created horse is returned with correct details.
   */
  @Test
  public void createHorseWithValidDataSucceeds() throws Exception {
    HorseCreateDto createDto = new HorseCreateDto(
        "Test Horse",
        "A test horse description",
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

    HorseDetailDto createdHorse = horseService.create(createDto, imageDto);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.id()).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("Test Horse");
    assertThat(createdHorse.description()).isEqualTo("A test horse description");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(LocalDate.of(2020, 1, 1));
    assertThat(createdHorse.sex()).isEqualTo(Sex.MALE);
    assertThat(createdHorse.owner()).isNull();
    assertThat(createdHorse.imageUrl()).isNotNull();
  }

  /**
   * Tests successful retrieval of an existing horse by ID.
   * Verifies that all fields contain the expected data.
   */
  @Test
  public void getHorseByIdWithExistingIdSucceeds() throws Exception {
    HorseDetailDto horse = horseService.getById(-1L);

    assertThat(horse).isNotNull();
    assertThat(horse.id()).isEqualTo(-1L);
    assertThat(horse.sex()).isEqualTo(Sex.MALE);
    assertThat(horse.name()).isNotNull();
    assertThat(horse.dateOfBirth()).isNotNull();
  }

  /**
   * Tests retrieval of a horse with a non-existing ID.
   * Verifies that a NotFoundException is thrown.
   */
  @Test
  public void getHorseByIdWithNonExistingIdFails() {
    assertThrows(NotFoundException.class, () -> {
      horseService.getById(999L);
    });
  }

  /**
   * Tests creation of a horse with invalid data (null name).
   * Verifies that a ValidationException is thrown.
   */
  @Test
  public void createHorseWithInvalidDataFails() {
    HorseCreateDto invalidDto = new HorseCreateDto(
        null,
        "Description",
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

    assertThrows(ValidationException.class, () -> {
      horseService.create(invalidDto, imageDto);
    });
  }

}
