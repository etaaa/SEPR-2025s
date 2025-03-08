package at.ac.tuwien.sepr.assignment.individual.persistence;

import jakarta.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/**
 * This component is only instantiated when the {@code datagen} profile is active.
 * It populates the database with test data upon initialization.
 * Activate this profile by adding {@code -Dspring.profiles.active=datagen} to your runtime arguments.
 */
@Component
@Profile("datagen")
public class DataGeneratorBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final DataSource dataSource;

  /**
   * Constructs the {@code DataGeneratorBean} with the required {@link DataSource}.
   *
   * @param dataSource the database connection source
   */
  public DataGeneratorBean(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Executes an SQL script to populate the database with test data upon bean initialization.
   *
   * @throws SQLException if an error occurs while executing the SQL script
   */
  @PostConstruct
  public void generateData() throws SQLException {
    LOGGER.info("Generating data...");
    try (var connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/insertData.sql"));
      LOGGER.info("Finished generating data successfully.");
    }
  }
}
