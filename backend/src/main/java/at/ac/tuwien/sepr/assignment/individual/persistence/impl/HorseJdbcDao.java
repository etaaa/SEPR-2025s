package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseImageDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
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

  private static final String SQL_SELECT_PARENT_BY_ID =
      "SELECT id, name "
          + "FROM " + TABLE_NAME
          + " WHERE id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, "
          + "CASE WHEN image IS NOT NULL THEN 1 ELSE 0 END AS has_image "
          + "FROM " + TABLE_NAME;

  private static final String SQL_SELECT_IMAGE_BY_ID =
      "SELECT image, mime_type FROM " + TABLE_NAME
          + " WHERE ID = :id";

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


  private final JdbcClient jdbcClient;

  @Autowired
  public HorseJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses = jdbcClient
        .sql(SQL_SELECT_ALL + " WHERE id = :id")
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

  @Override
  public HorseParentDto getParentById(long id) throws NotFoundException {
    /*
    As mentioned in the “Architekturdesign.pdf” slides on page 17, returning a
    DTO is acceptable if an entity doesn’t accurately represent the data. In
    this case, since we only need the ID and Name fields, our DTO represents the
    data more precisely. If we were to return an entity instead, many fields
    would be null, which is not ideal.
     */
    LOG.trace("getParentById({})", id);
    List<HorseParentDto> horses = jdbcClient
        .sql(SQL_SELECT_PARENT_BY_ID)
        .param("id", id)
        .query(this::mapParentRow)
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

  /*
  @Override
  public List<Horse> getAllById(Collection<Long> ids) {
    LOG.trace("getAllById({})", ids);
    return jdbcClient
        .sql(SQL_SELECT_ALL + " WHERE id IN (:ids)")
        .param("ids", ids)
        .query(this::mapRow)
        .list();
  }
  */

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
  public List<Horse> search(HorseSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);

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
    if (searchParameters.bornBefore() != null) {
      params.put("bornBefore", searchParameters.bornBefore());
      conditions.add("date_of_birth < :bornBefore");
    }
    if (searchParameters.sex() != null) {
      params.put("sex", searchParameters.sex().toString());
      conditions.add("sex = :sex");
    }
    // TODO: filter owners by name
    if (searchParameters.ownerName() != null) {
      params.put("ownerName", searchParameters.ownerName());
      conditions.add("ownerName = :ownerName");
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

    return jdbcClient
        .sql(query)
        .params(params)
        .query(this::mapRow)
        .list();
  }

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
        .param("mother_id", horse.motherId())
        .param("father_id", horse.fatherId())
        .param("image", horseImage == null ? null : horseImage.image())
        .param("mime_type", horseImage == null ? null : horseImage.mimeType())
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
        horse.ownerId(),
        horse.motherId(),
        horse.fatherId(),
        horseImage == null ? null : "/horses/" + id + "/image"
    );
  }


  @Override
  public Horse update(HorseUpdateDto horse, HorseImageDto horseImage) throws NotFoundException {
    // TODO: When we change the gender of a horse, all its children must remove the mother/father connection as that cant be correct anymore.
    LOG.trace("update({})", horse);

    if (horseImage == null && !horse.deleteImage()) {
      horseImage = getImageById(horse.id());
    }
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
        horse.ownerId(),
        horse.motherId(),
        horse.fatherId(),
        horseImage == null ? null : "/horses/" + horse.id() + "/image"
    );
  }


  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    int rowsAffected = jdbcClient
        .sql(SQL_DELETE_BY_ID)
        .param("id", id)
        .update();

    if (rowsAffected == 0) {
      throw new NotFoundException("No horse with ID " + id + " found for deletion");
    }
  }


  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
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

  private HorseParentDto mapParentRow(ResultSet result, int rownum) throws SQLException {

    return new HorseParentDto(
        result.getLong("id"),
        result.getString("name")
    );
  }

}
