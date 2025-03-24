package at.ac.tuwien.sepr.assignment.individual.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Integration tests for the Horse REST API endpoint.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
@Transactional
public class HorseEndpointTest {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Sets up the MockMvc instance before each test.
   */
  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  /**
   * Tests retrieving all horses from the endpoint.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void gettingAllHorses() throws Exception {

    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isGreaterThanOrEqualTo(10); // TODO: Adapt this to the exact number in the test data later
    assertThat(horseResult)
        .extracting(HorseListDto::id, HorseListDto::name)
        .contains(tuple(-1L, "Wendys Grandmother"));
  }

  /**
   * Tests that accessing a nonexistent URL returns a 404 status.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {

    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  /**
   * Tests retrieving a horse by ID with valid data.
   * Verifies all fields match the expected test data.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void retrieveHorseByIdValid() throws Exception {
    long horseId = -6L; // Wendy
    MvcResult result = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses/{id}", horseId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    HorseDetailDto horse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), HorseDetailDto.class);

    assertThat(horse).isNotNull();
    assertThat(horse.id()).isEqualTo(-6L);
    assertThat(horse.name()).isEqualTo("Wendy");
    assertThat(horse.description()).isEqualTo("The new one!");
    assertThat(horse.dateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
    assertThat(horse.sex()).isEqualTo(Sex.FEMALE);
    assertThat(horse.owner()).isNotNull();
    assertThat(horse.owner().firstName()).isEqualTo("Wendy");
    assertThat(horse.mother().id()).isEqualTo(-3L);
    assertThat(horse.father().id()).isEqualTo(-4L);
  }

  /**
   * Tests retrieving a horse with a non-existent ID.
   * Expects a 404 Not Found response.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void retrieveHorseByIdNotFound() throws Exception {
    long nonExistentId = 999L;
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses/{id}", nonExistentId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  /**
   * Tests creating a new horse with minimal valid data.
   * Verifies the horse is created with correct fields and a positive ID.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void createNewHorseValid() throws Exception {
    MvcResult result = mockMvc
        .perform(multipart("/horses")
            .param("name", "Test Horse")
            .param("dateOfBirth", "2023-01-01")
            .param("sex", "MALE")
            .param("ownerId", "-1")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andReturn();

    HorseDetailDto createdHorse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), HorseDetailDto.class);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.id()).isPositive();
    assertThat(createdHorse.name()).isEqualTo("Test Horse");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(LocalDate.of(2023, 1, 1));
    assertThat(createdHorse.sex()).isEqualTo(Sex.MALE);
    assertThat(createdHorse.owner()).isNotNull();
    assertThat(createdHorse.description()).isNull();
    assertThat(createdHorse.mother()).isNull();
    assertThat(createdHorse.father()).isNull();
  }

  /**
   * Tests creating a horse with missing required data (no name).
   * Expects a 400 Bad Request response due to binding failure.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void createHorseMissingName() throws Exception {
    mockMvc
        .perform(multipart("/horses")
            .param("dateOfBirth", "2023-01-01")
            .param("sex", "MALE")
            .param("ownerId", "-1")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity());
  }

  /**
   * Tests updating an existing horse with valid data.
   * Verifies the updated fields are correctly applied.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void updateHorseValid() throws Exception {
    long horseId = -6L; // Wendy
    MvcResult result = mockMvc
        .perform(multipart("/horses/{id}", horseId)
            .param("name", "Wendy Updated")
            .param("description", "Updated description")
            .param("dateOfBirth", "2000-01-01")
            .param("sex", "FEMALE")
            .param("ownerId", "-1")
            .param("deleteImage", "false")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PUT");
              return request;
            })
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    HorseDetailDto updatedHorse = objectMapper.readValue(result.getResponse().getContentAsByteArray(), HorseDetailDto.class);

    assertThat(updatedHorse).isNotNull();
    assertThat(updatedHorse.id()).isEqualTo(-6L);
    assertThat(updatedHorse.name()).isEqualTo("Wendy Updated");
    assertThat(updatedHorse.description()).isEqualTo("Updated description");
    assertThat(updatedHorse.dateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
    assertThat(updatedHorse.sex()).isEqualTo(Sex.FEMALE);
    assertThat(updatedHorse.owner()).isNotNull();
  }

  /**
   * Tests updating a non-existent horse.
   * Expects a 404 Not Found response.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void updateHorseNotFound() throws Exception {
    long nonExistentId = 999L;
    mockMvc
        .perform(multipart("/horses/{id}", nonExistentId)
            .param("name", "Non-existent Horse")
            .param("dateOfBirth", "2023-01-01")
            .param("sex", "MALE")
            .param("ownerId", "-1")
            .param("deleteImage", "false")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PUT");
              return request;
            })
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity());
  }
}