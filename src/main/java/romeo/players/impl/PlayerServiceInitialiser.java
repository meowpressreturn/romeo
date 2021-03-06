package romeo.players.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceInitialiser;
import romeo.players.api.PlayerId;
import romeo.players.api.PlayerUtils;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;

public class PlayerServiceInitialiser implements IServiceInitialiser {

  private IKeyGen _keyGen;
  
  public PlayerServiceInitialiser(IKeyGen keyGen) {
    _keyGen = Objects.requireNonNull(keyGen, "keyGen amy not be null");
  }
  
  /**
   * If the PLAYERS table doesnt exit then create it. Update existing one if
   * necessary.
   * @param tableNames
   * @param connection
   */
  @Override
  public void init(Set<String> tableNames, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    if(!tableNames.contains("PLAYERS")) {
      final String sql = "CREATE TABLE PLAYERS (" 
          + "id VARCHAR NOT NULL PRIMARY KEY" 
          + ",name VARCHAR DEFAULT '' NOT NULL" 
          + ",nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE"
          + ",status VARCHAR DEFAULT '' NOT NULL"
          + ",notes VARCHAR DEFAULT '' NOT NULL" 
          + ",color VARCHAR DEFAULT '" + Convert.toStr(PlayerUtils.SOMEBODY_COLOR) + "' NOT NULL" 
          + ",team VARCHAR DEFAULT '' NOT NULL"
          + ");";
      log.info("Creating PLAYERS table");
      DbUtils.writeQuery(sql, null, connection);
      
      //For ancient databases, a special porting of owners and colors into player records from the ancient worlds schema
      //nb: tableNames won't contain this table since we just created it back in the world service initialiser
      if( DbUtils.getTableNames(connection).contains("ANCIENT_COLORS") ) {
        importPlayersFromAncientColors(connection);
      }      
    } else {
      if(DbUtils.fixEmptyColumn("PLAYERS", "name", "Untitled", connection)) {
        log.info("Changed empty names to 'Untitled'");
      }
      //Ensure all the names are trimmed and unique. (We found that UC actually has some nobody world names with extra whitespace and that
      //it allows this for player homeworld names too)
      if( DbUtils.ensureTrimmed("PLAYERS", "name", connection) ) {
        log.info("Trimmed whitespace from names");
      }
      if( DbUtils.fixDuplicates("PLAYERS", "name", connection) ) {
        log.info("Fixed duplicate values in names");
      }
    }

    Set<String> columnNames = DbUtils.getColumnNames(connection, "PLAYERS");
    Set<String> nullableColumns = DbUtils.getNullableColumnNames(connection, "PLAYERS");
    log.debug("Existing columns in PLAYERS table=" + columnNames);
    log.debug("Nullable columns in PLAYERS table=" + nullableColumns);
    updateColumns(connection, columnNames, nullableColumns);
  }
  
  private void importPlayersFromAncientColors(Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    try {
      ResultSet rs = DbUtils.readQuery("SELECT owner, color FROM ANCIENT_COLORS",null, connection);
      while(rs.next()) {
        PlayerId id = new PlayerId(_keyGen.createIdKey());
        String name = rs.getString("owner");
        String color = rs.getString("color");
        log.info("Importing player " + name + " with color " + color);
        DbUtils.writeQuery("INSERT INTO PLAYERS (id, name, color) VALUES (?,?,?)", new Object[] { id, name, color}, connection);
      }
      log.info("Removing the temporary ANCIENT_COLORS table");
      DbUtils.writeQuery("DROP TABLE ANCIENT_COLORS", null, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error importing players from ancient colors");
    }
  }
  
  private void updateColumns(Connection connection, Set<String> columnNames, Set<String> nullableColumns) {
    Log log = LogFactory.getLog(this.getClass());
    if(!columnNames.contains("TEAM")) {
      log.info("Adding team column to PLAYERS table");
      DbUtils.writeQuery("ALTER TABLE PLAYERS ADD COLUMN team VARCHAR DEFAULT '' NOT NULL", null, connection);
    }
    
    //As of 0.6.3 we have made players columns not nullable
    if(nullableColumns.contains("NAME")) {
      log.info("Modifying columns to be non-nullable");
      DbUtils.makeNonNull("PLAYERS", "''", connection, "name","status","notes","color","team");
    }    
    
    if(!columnNames.contains("NAMELOOKUP")) {
      //This will enforce unique names in a case-insensitive manner, and is also used to join in case-insensitive way
      //such as done by the playersSummary query
      log.info("Creating unique uppercase nameLookup column");
      DbUtils.writeQuery(
          "ALTER TABLE PLAYERS ADD COLUMN nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE", null, connection);
//      DbUtils.writeQuery("ALTER TABLE PLAYERS ALTER COLUMN nameLookup SET DEFAULT ''", null, connection);
//      DbUtils.writeQuery("ALTER TABLE PLAYERS ALTER COLUMN nameLookup SET NOT NULL", null, connection);
    }
  }

  /**
   * Delete all data in the PLAYERS table
   * @param connection
   */
  @Override
  public void reset(Connection connection) {
    LogFactory.getLog(this.getClass()).debug("Deleting data in PLAYERS table");
    DbUtils.writeQuery("DELETE FROM PLAYERS;", null, connection);
  }
}
