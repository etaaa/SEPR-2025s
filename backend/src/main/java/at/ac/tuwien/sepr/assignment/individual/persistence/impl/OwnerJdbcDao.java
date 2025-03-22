package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link OwnerDao} for interacting with the database.
 */
@Repository
public class OwnerJdbcDao implements OwnerDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "owner";

  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
          + " WHERE id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME;

  private static final String SQL_INSERT =
      "INSERT INTO " + TABLE_NAME
          + " (first_name, last_name, description) "
          + "VALUES (:firstName, :lastName, :description)";

  private final JdbcClient jdbcClient;

  @Autowired
  public OwnerJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }


  @Override
  public Owner getById(long id) throws NotFoundException {

    LOG.trace("getById({})", id);

    try {
      List<Owner> owners = jdbcClient
          .sql(SQL_SELECT_BY_ID)
          .param("id", id)
          .query(this::mapRow)
          .list();

      if (owners.isEmpty()) {
        throw new NotFoundException("Owner with ID %d not found".formatted(id));
      }
      if (owners.size() > 1) {
        throw new FatalException("Found more than one owner with ID %d".formatted(id));
      }
      return owners.getFirst();

    } catch (DataAccessException e) {
      throw new PersistenceException("Error accessing database", e);
    }
  }


  @Override
  public List<Owner> getAll() {

    LOG.trace("getAll()");

    try {
      return jdbcClient
          .sql(SQL_SELECT_ALL)
          .query(this::mapRow)
          .list();

    } catch (DataAccessException e) {
      throw new PersistenceException("Error accessing database", e);
    }
  }


  @Override
  public List<Owner> search(OwnerSearchDto searchParameters) {

    LOG.trace("search({})", searchParameters);

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
      return jdbcClient
          .sql(query)
          .params(params)
          .query(this::mapRow)
          .list();

    } catch (DataAccessException e) {
      throw new PersistenceException("Error accessing database", e);
    }
  }


  private Owner mapRow(ResultSet resultSet, int rowNum) throws SQLException {

    return new Owner(
        resultSet.getLong("id"),
        resultSet.getString("first_name"),
        resultSet.getString("last_name"),
        resultSet.getString("description"));
  }

}
