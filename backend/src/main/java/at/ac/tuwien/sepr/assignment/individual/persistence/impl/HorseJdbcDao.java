package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE ID = :id";

  // TODO
  private static final String SQL_SELECT_IMAGE_BY_ID =
      "SELECT image, mime_type FROM " + TABLE_NAME
          + " WHERE ID = :id";

  private static final String SQL_UPDATE =
      "UPDATE " + TABLE_NAME
          + """
              SET name = :name,
                  description = :description,
                  date_of_birth = :date_of_birth,
                  sex = :sex
              WHERE id = :id
          """;

  // TODO
  private static final String SQL_INSERT =
      "INSERT INTO " + TABLE_NAME
          + " (name, description, date_of_birth, sex, owner_id, image, mime_type) "
          + "VALUES (:name, :description, :date_of_birth, :sex, :owner_id, :image, :mime_type)";


  private final JdbcClient jdbcClient;

  @Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .query(this::mapRow)
        .list();
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses = jdbcClient
        .sql(SQL_SELECT_BY_ID)
        .param("id", id)
        .query(this::mapRow)
        .list();

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.getFirst();
  }


  // TODO
  @Override
  public HorseImageDto getImageById(long id) throws NotFoundException {
    List<HorseImageDto> images = jdbcClient
        .sql(SQL_SELECT_IMAGE_BY_ID)
        .param("id", id)
        .query((rs, rowNum) -> new HorseImageDto(rs.getBytes("image"), rs.getString("mime_type")))
        .list();

    if (images.isEmpty() || images.get(0) == null) {
      throw new NotFoundException("No image for horse with ID " + id + " found.");
    }

    return images.get(0);
  }


  @Override
  public Horse update(HorseUpdateDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcClient
        .sql(SQL_UPDATE)
        .param("id", horse.id())
        .param("name", horse.name())
        .param("description", horse.description())
        .param("date_of_birth", horse.dateOfBirth())
        .param("sex", horse.sex().toString())
        .param("owner_id", horse.ownerId())
        .update();

    if (updated == 0) {
      throw new NotFoundException(
          "Could not update horse with ID " + horse.id() + ", because it does not exist"
      );
    }

    return new Horse(
        horse.id(),
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        horse.ownerId());
  }


  // TODO
  @Override
  public Horse create(HorseCreateDto horse, HorseImageDto horseImage) {
    LOG.trace("create({})", horse);

    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rowsAffected = jdbcClient.sql(SQL_INSERT)
        .param("name", horse.name())
        .param("description", horse.description())
        .param("date_of_birth", horse.dateOfBirth())
        .param("sex", horse.sex().toString())
        .param("owner_id", horse.ownerId())
        .param("image", horseImage.image())
        .param("mime_type", horseImage.mimeType())
        .update(keyHolder);

    if (rowsAffected == 0 || keyHolder.getKey() == null) {
      throw new RuntimeException("Failed to insert horse into database");
    }

    Long id = keyHolder.getKey().longValue();

    return new Horse(
        id,
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex(),
        horse.ownerId()
    );
  }


  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse(
        result.getLong("id"),
        result.getString("name"),
        result.getString("description"),
        result.getDate("date_of_birth").toLocalDate(),
        Sex.valueOf(result.getString("sex")),
        result.getObject("owner_id", Long.class));
  }

}
