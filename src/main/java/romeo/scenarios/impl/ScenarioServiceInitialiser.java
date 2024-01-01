package romeo.scenarios.impl;

import java.sql.Connection;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;

import romeo.model.api.IServiceInitialiser;
import romeo.utils.DbUtils;

public class ScenarioServiceInitialiser implements IServiceInitialiser {

  private final Logger _log;
  
  public ScenarioServiceInitialiser(Logger log) {
    _log = Objects.requireNonNull(log, "log may not be null");
  }
  
  @Override
  public void init(Set<String> tableNames, Connection connection) {
    if(!tableNames.contains("SCENARIOS")) {
      String sql = "CREATE TABLE SCENARIOS (id VARCHAR NOT NULL PRIMARY KEY" + ",name VARCHAR DEFAULT '' NOT NULL"
          + ",fleetsCsv VARCHAR DEFAULT '' NOT NULL);";
      _log.info("Creating SCENARIOS table");
      DbUtils.writeQuery(sql, null, connection);
    } else {
      _log.info("SCENARIOS table exists - skipping initalisation");
    }
  }

  /**
   * Erase all scenario data in the db
   * @param connection
   */
  @Override
  public void reset(Connection connection) {
    DbUtils.writeQuery("DELETE FROM SCENARIOS;", null, connection);
  }

}
