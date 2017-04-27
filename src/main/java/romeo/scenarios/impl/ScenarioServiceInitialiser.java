package romeo.scenarios.impl;

import java.sql.Connection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceInitialiser;
import romeo.utils.DbUtils;

public class ScenarioServiceInitialiser implements IServiceInitialiser {

  @Override
  public void init(Set<String> tableNames, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    if(!tableNames.contains("SCENARIOS")) {
      String sql = "CREATE TABLE SCENARIOS (id VARCHAR NOT NULL PRIMARY KEY" + ",name VARCHAR DEFAULT '' NOT NULL"
          + ",fleetsCsv VARCHAR DEFAULT '' NOT NULL);";
      log.info("Creating SCENARIOS table");
      DbUtils.writeQuery(sql, null, connection);
    } else {
      log.info("SCENARIOS table exists - skipping initalisation");
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
