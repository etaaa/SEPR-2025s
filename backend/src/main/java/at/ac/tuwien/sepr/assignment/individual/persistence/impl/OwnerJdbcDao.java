package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.PersistenceException;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;

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
 * JDBC implementation of {@link OwnerDao} for interacting with the database.
 */
@Repository
public class OwnerJdbcDao implements OwnerDao {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "owner";
  private final JdbcClient jdbcClient;

  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_INSERT =
      "INSERT INTO " + TABLE_NAME
          + " (first_name, last_name, description) "
          + "VALUES (:firstName, :lastName, :description)";

  @Autowired
  public OwnerJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Owner getById(long id) throws NotFoundException {

    LOG.trace("Entering getById [requestId={}]: Retrieving owner with id {}", MDC.get("r"), id);

    try {
      List<Owner> owners = jdbcClient
          .sql(SQL_SELECT_BY_ID)
          .param("id", id)
          .query(this::mapRow)
          .list();

      if (owners.isEmpty()) {
        LOG.warn("Owner with ID {} not found [requestId={}]", id, MDC.get("r"));

        throw new NotFoundException("Owner with ID %d not found".formatted(id));
      }
      if (owners.size() > 1) {
        LOG.error("Unexpected error [requestId={}]: Found multiple owners with ID {}", MDC.get("r"), id);

        throw new FatalException("Found more than one owner with ID %d".formatted(id));
      }

      LOG.debug("Retrieved owner with id {} [requestId={}]: {}", id, MDC.get("r"), owners.getFirst());

      return owners.getFirst();

    } catch (DataAccessException e) {
      LOG.error("Database access failed [requestId={}]: Error retrieving owner with id {}", MDC.get("r"), id, e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Owner> getAll() {

    LOG.trace("Entering getAll [requestId={}]: Retrieving all owners", MDC.get("r"));

    try {
      List<Owner> owners = jdbcClient
          .sql(SQL_SELECT_ALL)
          .query(this::mapRow)
          .list();

      LOG.debug("Retrieved {} owners [requestId={}]", owners.size(), MDC.get("r"));

      return owners;

    } catch (DataAccessException e) {
      LOG.error("Database access failed [requestId={}]: Error retrieving all owners", MDC.get("r"), e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Owner> search(OwnerSearchDto searchParameters) {

    LOG.trace("Entering search [requestId={}]: Searching owners with parameters {}", MDC.get("r"), searchParameters);

    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    if (searchParameters.name() != null) {
      params.put("name", searchParameters.name());
      conditions.add("UPPER(first_name || ' ' || last_name) LIKE UPPER('%%' || COALESCE(:name, '') || '%%')");
    }

    if (searchParameters.ids() != null) {
      params.put("ids", searchParameters.ids());
      conditions.add("id IN (:ids)");
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
      List<Owner> owners = jdbcClient
          .sql(query)
          .params(params)
          .query(this::mapRow)
          .list();

      LOG.debug("Found {} owners matching search parameters [requestId={}]", owners.size(), MDC.get("r"));

      return owners;

    } catch (DataAccessException e) {
      LOG.error("Database access failed [requestId={}]: Error searching owners with parameters {}", MDC.get("r"), searchParameters, e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Owner create(OwnerCreateDto owner) {

    LOG.trace("Entering create [requestId={}]: Creating owner with data {}", MDC.get("r"), owner);

    KeyHolder keyHolder = new GeneratedKeyHolder();

    try {
      int rowsAffected = jdbcClient.sql(SQL_INSERT)
          .param("firstName", owner.firstName())
          .param("lastName", owner.lastName())
          .param("description", owner.description())
          .update(keyHolder);

      if (rowsAffected == 0 || keyHolder.getKey() == null) {
        LOG.error("Unexpected error [requestId={}]: Failed to insert owner into database, no rows affected or no key returned", MDC.get("r"));

        throw new RuntimeException("Failed to insert owner into database");
      }

      Long id = keyHolder.getKey().longValue();
      Owner createdOwner = new Owner(id, owner.firstName(), owner.lastName(), owner.description());

      LOG.info("Successfully created owner with id {} [requestId={}]", id, MDC.get("r"));

      return createdOwner;

    } catch (DataAccessException e) {
      LOG.error("Database access failed [requestId={}]: Error creating owner with data {}", MDC.get("r"), owner, e);

      throw new PersistenceException("Error accessing database", e);
    }
  }

  /**
   * Maps a database row to an {@link Owner} entity.
   *
   * @param resultSet the result set containing the row data
   * @param rowNum    the row number in the result set
   * @return an {@link Owner} entity constructed from the row data
   * @throws SQLException if an error occurs while accessing the result set
   */
  private Owner mapRow(ResultSet resultSet, int rowNum) throws SQLException {

    return new Owner(
        resultSet.getLong("id"),
        resultSet.getString("first_name"),
        resultSet.getString("last_name"),
        resultSet.getString("description"));
  }
}