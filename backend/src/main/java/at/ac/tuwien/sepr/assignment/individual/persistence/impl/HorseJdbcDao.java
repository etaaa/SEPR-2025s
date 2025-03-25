package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.PersistenceException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link HorseDao} for interacting with the database.
 */
@Repository
public class HorseJdbcDao implements HorseDao {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "horse";
  private final JdbcClient jdbcClient;

  private static final String SQL_SELECT_IMAGE_BY_ID =
      "SELECT image, mime_type FROM " + TABLE_NAME
          + " WHERE ID = :id";

  private static final String SQL_SELECT_PARENT_BY_ID =
      "SELECT id, name "
          + "FROM " + TABLE_NAME
          + " WHERE id = :id";

  private static final String SQL_SELECT_CHILDREN_BY_ID =
      "SELECT id, date_of_birth, sex "
          + "FROM " + TABLE_NAME
          + " WHERE mother_id = :id OR father_id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, "
          + "CASE WHEN image IS NOT NULL THEN 1 ELSE 0 END AS has_image "
          + "FROM " + TABLE_NAME;

  private static final String SQL_INSERT =
      "INSERT INTO " + TABLE_NAME
          + " (name, description, date_of_birth, sex, owner_id, mother_id, father_id, image, mime_type) "
          + "VALUES (:name, :description, :date_of_birth, :sex, :owner_id, :mother_id, :father_id, :image, :mime_type)";

  private static final String SQL_UPDATE_BY_ID =
      "UPDATE " + TABLE_NAME
          + """
              SET name = :name,
                  description = :description,
                  date_of_birth = :date_of_birth,
                  sex = :sex,
                  owner_id = :owner_id,
                  mother_id = :mother_id,
                  father_id = :father_id,
                  image = :image,
                  mime_type = :mime_type
              WHERE id = :id
          """;

  private static final String SQL_DELETE_BY_ID =
      "DELETE FROM " + TABLE_NAME
          + " WHERE id = :id";

  @Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Horse getById(long id) throws NotFoundException {

    LOG.trace("Entering getById [requestId={}]: Retrieving horse with id {}", MDC.get("r"), id);

    try {
      List<Horse> horses = jdbcClient
          .sql(SQL_SELECT_ALL + " WHERE id = :id")
          .param("id", id)
          .query(this::mapRow)
          .list();

      if (horses.isEmpty()) {
        LOG.warn("Horse with ID {} not found [requestId={}]", id, MDC.get("r"));

        throw new NotFoundException("No horse with ID %d found".formatted(id));
      }
      if (horses.size() > 1) {
        LOG.error("Multiple horses with ID {} found [requestId={}]", id, MDC.get("r"));

        throw new FatalException("Too many horses with ID %d found".formatted(id));
      }

      LOG.debug("Retrieved horse with ID {} [requestId={}]: {}", id, MDC.get("r"), horses.getFirst());

      return horses.getFirst();

    } catch (DataAccessException e) {
      LOG.error("Database access failed for getById with ID {} [requestId={}]: {}", id, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseImageDto getImageById(long id) throws NotFoundException {

    LOG.trace("Entering getImageById [requestId={}]: Retrieving image for horse with id {}", MDC.get("r"), id);

    try {
      List<HorseImageDto> images = jdbcClient
          .sql(SQL_SELECT_IMAGE_BY_ID)
          .param("id", id)
          .query((rs, rowNum) -> new HorseImageDto(rs.getBytes("image"), rs.getString("mime_type")))
          .list();

      if (images.isEmpty() || images.get(0) == null) {
        LOG.warn("No image found for horse with ID {} [requestId={}]", id, MDC.get("r"));

        throw new NotFoundException("No image for horse with ID %d found".formatted(id));
      }
      if (images.size() > 1) {
        LOG.error("Multiple images found for horse with ID {} [requestId={}]", id, MDC.get("r"));

        throw new FatalException("Too many horses with ID %d found".formatted(id));
      }

      LOG.debug("Retrieved image for horse ID {} [requestId={}]: MIME type {}", id, MDC.get("r"), images.get(0).mimeType());

      return images.get(0);

    } catch (DataAccessException e) {
      LOG.error("Database access failed for getImageById with ID {} [requestId={}]: {}", id, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HorseParentDto getParentById(long id) throws NotFoundException {

    /*
    As mentioned in the “Architekturdesign.pdf” slides on page 17, returning a
    DTO is acceptable if an entity doesn't accurately represent the data. In
    this case, since we only need the ID and Name fields, our DTO represents the
    data more precisely. If we were to return an entity instead, many fields
    would be null, which is not ideal.
     */
    LOG.trace("Entering getParentById [requestId={}]: Retrieving parent with id {}", MDC.get("r"), id);

    try {
      List<HorseParentDto> horses = jdbcClient
          .sql(SQL_SELECT_PARENT_BY_ID)
          .param("id", id)
          .query(this::mapParentRow)
          .list();

      if (horses.isEmpty()) {
        LOG.warn("Parent with ID {} not found [requestId={}]", id, MDC.get("r"));

        throw new NotFoundException("No horse with ID %d found".formatted(id));
      }
      if (horses.size() > 1) {
        LOG.error("Multiple parents with ID {} found [requestId={}]", id, MDC.get("r"));

        throw new FatalException("Too many horses with ID %d found".formatted(id));
      }

      LOG.debug("Retrieved parent with ID {} [requestId={}]: {}", id, MDC.get("r"), horses.getFirst());

      return horses.getFirst();

    } catch (DataAccessException e) {
      LOG.error("Database access failed for getParentById with ID {} [requestId={}]: {}", id, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Horse> getChildrenByParentId(long id) {

    LOG.trace("Entering getChildrenByParentId [requestId={}]: Retrieving children for parent id {}", MDC.get("r"), id);

    try {
      List<Horse> children = jdbcClient
          .sql(SQL_SELECT_CHILDREN_BY_ID)
          .param("id", id)
          .query(this::mapChildRow)
          .list();

      LOG.debug("Retrieved {} children for parent ID {} [requestId={}]", children.size(), id, MDC.get("r"));

      return children;

    } catch (DataAccessException e) {
      LOG.error("Database access failed for getChildrenByParentId with ID {} [requestId={}]: {}", id, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Horse> getAll() {

    LOG.trace("Entering getAll [requestId={}]: Retrieving all horses", MDC.get("r"));

    try {
      List<Horse> horses = jdbcClient
          .sql(SQL_SELECT_ALL)
          .query(this::mapRow)
          .list();

      LOG.debug("Retrieved {} horses [requestId={}]", horses.size(), MDC.get("r"));

      return horses;

    } catch (DataAccessException e) {
      LOG.error("Database access failed for getAll [requestId={}]: {}", MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Horse> search(HorseSearchDto searchParameters) {

    LOG.trace("Entering search [requestId={}]: Searching horses with parameters {}", MDC.get("r"), searchParameters);

    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    if (searchParameters.name() != null) {
      params.put("name", "%" + searchParameters.name() + "%");
      conditions.add("LOWER(name) LIKE LOWER(:name)");
    }
    if (searchParameters.description() != null) {
      params.put("description", "%" + searchParameters.description() + "%");
      conditions.add("LOWER(description) LIKE LOWER(:description)");
    }
    if (searchParameters.dateOfBirth() != null) {
      params.put("dateOfBirth", searchParameters.dateOfBirth());
      conditions.add("date_of_birth < :dateOfBirth");
    }
    if (searchParameters.sex() != null) {
      params.put("sex", searchParameters.sex().toString());
      conditions.add("sex = :sex");
    }
    if (searchParameters.excludeId() != null) {
      params.put("excludeId", searchParameters.excludeId());
      conditions.add("id <> :excludeId");
    }

    String query = SQL_SELECT_ALL;
    if (!conditions.isEmpty()) {
      query += " WHERE " + String.join(" AND ", conditions);
    }
    if (searchParameters.limit() != null) {
      params.put("limit", searchParameters.limit());
      query += " LIMIT :limit";
    }

    try {
      List<Horse> horses = jdbcClient
          .sql(query)
          .params(params)
          .query(this::mapRow)
          .list();

      LOG.debug("Found {} horses matching search parameters [requestId={}]", horses.size(), MDC.get("r"));

      return horses;

    } catch (DataAccessException e) {
      LOG.error("Database access failed for search with parameters {} [requestId={}]: {}", searchParameters, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Horse create(HorseCreateDto horse, HorseImageDto horseImage) {

    LOG.trace("Entering create [requestId={}]: Creating horse with data {}", MDC.get("r"), horse);

    KeyHolder keyHolder = new GeneratedKeyHolder();

    try {
      int rowsAffected = jdbcClient.sql(SQL_INSERT)
          .param("name", horse.name())
          .param("description", horse.description())
          .param("date_of_birth", horse.dateOfBirth())
          .param("sex", horse.sex().toString())
          .param("owner_id", horse.ownerId())
          .param("mother_id", horse.motherId())
          .param("father_id", horse.fatherId())
          .param("image", horseImage == null ? null : horseImage.image())
          .param("mime_type", horseImage == null ? null : horseImage.mimeType())
          .update(keyHolder);

      if (rowsAffected == 0 || keyHolder.getKey() == null) {
        LOG.error("Failed to insert horse into database [requestId={}]: No rows affected or key not generated", MDC.get("r"));

        throw new PersistenceException("Failed to insert horse into database");
      }

      Long id = keyHolder.getKey().longValue();
      Horse createdHorse = new Horse(
          id, horse.name(), horse.description(), horse.dateOfBirth(), horse.sex(),
          horse.ownerId(), horse.motherId(), horse.fatherId(),
          horseImage == null ? null : "/horses/" + id + "/image"
      );

      LOG.info("Successfully created horse with ID {} [requestId={}]", id, MDC.get("r"));

      return createdHorse;

    } catch (DataAccessException e) {
      LOG.error("Database access failed for create with data {} [requestId={}]: {}", horse, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Horse update(HorseUpdateDto horse, HorseImageDto horseImage) throws NotFoundException {

    LOG.trace("Entering update [requestId={}]: Updating horse with id {} and data {}", MDC.get("r"), horse.id(), horse);

    if (horseImage == null && !horse.deleteImage()) {
      horseImage = getImageById(horse.id());
    }

    try {
      int updated = jdbcClient
          .sql(SQL_UPDATE_BY_ID)
          .param("id", horse.id())
          .param("name", horse.name())
          .param("description", horse.description())
          .param("date_of_birth", horse.dateOfBirth())
          .param("sex", horse.sex().toString())
          .param("owner_id", horse.ownerId())
          .param("mother_id", horse.motherId())
          .param("father_id", horse.fatherId())
          .param("image", horseImage == null ? null : horseImage.image())
          .param("mime_type", horseImage == null ? null : horseImage.mimeType())
          .update();

      if (updated == 0) {
        LOG.warn("No horse with ID {} found to update [requestId={}]", horse.id(), MDC.get("r"));

        throw new NotFoundException("No horse with ID " + horse.id() + " found to update");
      }

      Horse updatedHorse = new Horse(
          horse.id(), horse.name(), horse.description(), horse.dateOfBirth(), horse.sex(),
          horse.ownerId(), horse.motherId(), horse.fatherId(),
          horseImage == null ? null : "/horses/" + horse.id() + "/image"
      );

      LOG.info("Successfully updated horse with ID {} [requestId={}]", horse.id(), MDC.get("r"));

      return updatedHorse;

    } catch (DataAccessException e) {
      LOG.error("Database access failed for update with ID {} [requestId={}]: {}", horse.id(), MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(long id) throws NotFoundException {

    LOG.trace("Entering delete [requestId={}]: Deleting horse with id {}", MDC.get("r"), id);

    try {
      int rowsAffected = jdbcClient
          .sql(SQL_DELETE_BY_ID)
          .param("id", id)
          .update();

      if (rowsAffected == 0) {
        LOG.warn("No horse with ID {} found for deletion [requestId={}]", id, MDC.get("r"));

        throw new NotFoundException("No horse with ID " + id + " found for deletion");
      }

      LOG.info("Successfully deleted horse with ID {} [requestId={}]", id, MDC.get("r"));

    } catch (DataAccessException e) {
      LOG.error("Database access failed for delete with ID {} [requestId={}]: {}", id, MDC.get("r"), e.getMessage(), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * Maps a database result set row to a {@link Horse} entity.
   *
   * @param result the result set containing horse data
   * @param rowNum the current row number
   * @return the mapped {@link Horse} entity
   * @throws SQLException if an error occurs while accessing the result set
   */
  private Horse mapRow(ResultSet result, int rowNum) throws SQLException {

    long id = result.getLong("id");
    boolean hasImage = result.getInt("has_image") == 1;
    String imageUrl = hasImage ? "/horses/" + id + "/image" : null;

    return new Horse(
        id,
        result.getString("name"),
        result.getString("description"),
        result.getDate("date_of_birth").toLocalDate(),
        Sex.valueOf(result.getString("sex")),
        result.getObject("owner_id", Long.class),
        result.getObject("mother_id", Long.class),
        result.getObject("father_id", Long.class),
        imageUrl
    );
  }

  /**
   * Maps a database result set row to a {@link HorseParentDto} object.
   *
   * @param result the result set containing parent horse data
   * @param rowNum the current row number
   * @return the mapped {@link HorseParentDto} object
   * @throws SQLException if an error occurs while accessing the result set
   */
  private HorseParentDto mapParentRow(ResultSet result, int rowNum) throws SQLException {

    return new HorseParentDto(
        result.getLong("id"),
        result.getString("name")
    );
  }

  /**
   * Maps a database result set row to a minimal {@link Horse} entity for child horses.
   *
   * @param result the result set containing child horse data
   * @param rowNum the current row number
   * @return the mapped {@link Horse} entity with limited fields
   * @throws SQLException if an error occurs while accessing the result set
   */
  private Horse mapChildRow(ResultSet result, int rowNum) throws SQLException {

    return new Horse(
        result.getLong("id"),
        null,
        null,
        result.getDate("date_of_birth").toLocalDate(),
        Sex.valueOf(result.getString("sex")),
        null,
        null,
        null,
        null
    );
  }
}