package romeo.worlds.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceInitialiser;
import romeo.players.api.IPlayer;
import romeo.players.api.PlayerUtils;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;

/**
 * Initialise the WORLDS table in the db if necessary.
 */
public class WorldServiceInitialiser implements IServiceInitialiser {
  
  private IKeyGen _keyGen;
  private ISettingsService _settingsService;

  public WorldServiceInitialiser(IKeyGen keyGen, ISettingsService settingsService) {
    _keyGen = Objects.requireNonNull(keyGen, "keyGen may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
  }
  
  /**
   * If the WORLDS table doesnt exit then create it. Update existing one if
   * necessary.
   * @param tableNames
   * @param connection
   */
  @Override
  public void init(Set<String> tableNames, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    if(!tableNames.contains("WORLDS")) {
      final String sql = "CREATE TABLE WORLDS (" 
          + "id VARCHAR NOT NULL PRIMARY KEY" 
          + ",name VARCHAR DEFAULT '' NOT NULL" 
          + ",nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(NAME)) UNIQUE"
          + ",worldX INTEGER DEFAULT 0 NOT NULL"
          + ",worldY INTEGER DEFAULT 0 NOT NULL" 
          + ",notes VARCHAR DEFAULT '' NOT NULL" 
          + ",worldEi INTEGER DEFAULT 0 NOT NULL" 
          + ",worldRer INTEGER DEFAULT 0 NOT NULL" 
          + ",scannerId VARCHAR" //fk to units, may be null
          + ");";
      log.info("Creating WORLDS table");
      DbUtils.writeQuery(sql, null, connection);
    } else {
      if(DbUtils.fixEmptyColumn("WORLDS", "name", "Untitled", connection)) {
        log.info("Changed empty names to 'Untitled'");
      }
      //Ensure all the names are trimmed and unique. (We found that UC actually has some nobody world names with extra whitespace and that
      //it allows this for player homeworld names too)
      if( DbUtils.ensureTrimmed("WORLDS", "name", connection) ) {
        log.info("Trimmed whitespace from names");
      }
      if( DbUtils.fixDuplicates("WORLDS", "name", connection) ) {
        log.info("Fixed duplicate values in names");
      }
    }

    if(!tableNames.contains("WORLDS_HISTORY")) {
      log.info("Creating WORLDS_HISTORY table");
      final String sql = "CREATE TABLE WORLDS_HISTORY (" 
          + "worldId VARCHAR NOT NULL" 
          + ",turn INTEGER DEFAULT 1 NOT NULL" //0 is not a valid turn
          + ",owner VARCHAR DEFAULT '' NOT NULL" 
          + ",firepower DOUBLE DEFAULT 0 NOT NULL" 
          + ",labour INTEGER DEFAULT 0 NOT NULL" 
          + ",capital INTEGER DEFAULT 0 NOT NULL"
          + ",PRIMARY KEY (worldId,turn)" + ");";
      DbUtils.writeQuery(sql, null, connection);
    }

    Set<String> worldColumns = DbUtils.getColumnNames(connection, "WORLDS");
    Set<String> nullableWorldColumns = DbUtils.getNullableColumnNames(connection, "WORLDS");
    log.debug("Existing columns in WORLDS table=" + worldColumns);
    log.debug("Nullable columns in WORLDS table=" + nullableWorldColumns);
    updateWorldColumns(connection, tableNames, worldColumns, nullableWorldColumns);
    
    Set<String> historyColumns = DbUtils.getColumnNames(connection, "WORLDS_HISTORY");
    Set<String> nullableHistoryColumns = DbUtils.getNullableColumnNames(connection, "WORLDS_HISTORY");
    log.debug("Existing columns in WORLDS_HISTORY table=" + historyColumns);
    updateWorldsHistoryColumns(connection, historyColumns, nullableHistoryColumns);
  }

  /**
   * Create columns that were not present in earlier versions of Romeo and update definitions.
   * @param connection
   * @param worldColumns
   */
  private void updateWorldColumns(Connection connection, Set<String> tableNames, Set<String> worldColumns,
      Set<String> nullableWorldColumns) {
    Log log = LogFactory.getLog(this.getClass());
    try {
      //World EI and RER columns were added around 0.2.0
      if(!worldColumns.contains("WORLDEI")) {
        log.info("Adding worldEi column to WORLDS table");
        DbUtils.writeQuery("ALTER TABLE WORLDS ADD COLUMN worldEi INTEGER DEFAULT 0 NOT NULL", null, connection);
      }
      if(!worldColumns.contains("WORLDRER")) {
        log.info("Adding worldRer column to WORLDS table");
        DbUtils.writeQuery("ALTER TABLE WORLDS ADD COLUMN worldRer INTEGER DEFAULT 0 NOT NULL", null, connection);
      }
      
      boolean addedOwner = false; //If we add it here we will want to delete it again      
      boolean addedFirepower= false; //If we add it here we will want to delete it again

      boolean worldHasColorColumn = worldColumns.contains("COLOR");
      boolean worldsHasOwnerColumn = worldColumns.contains("OWNER");
      boolean playersTableExists = tableNames.contains("PLAYERS");
      boolean assignOwnersFromAncientColors = false;
      if(worldHasColorColumn && !playersTableExists) {
        //Port colors from databases which used a color field in worlds (now we use the player's color,
        //but in 0.5.2 and earlier there was such a field in Worlds. In 0.5.2 we also had the one in Players
        //which back then was used as a default, and the worlds could still vary independently)
        //To port in, we will copy colors and owners (owners were in worlds between 0.2.x and 0.5.2, but not in 0.1.0)
        //into a temp table where the player service initialiser can pick them up and create player records from them.
        //If there is a players table (eg 0.5.x) then don't do this as the worlds can get their colors from the players naturally.
        log.info("Creating temporary table to port ancient owner, color information");
        final String sql = "CREATE TABLE ANCIENT_COLORS (" 
            + "owner VARCHAR DEFAULT '' NOT NULL" 
            + ",color VARCHAR DEFAULT '' NOT NULL" 
            + ");";
        DbUtils.writeQuery(sql, null, connection);   
        String nobodyColor = Convert.toStr( PlayerUtils.NOBODY_COLOR );
        
        if(!worldsHasOwnerColumn) {
          //In 0.1.0 there was only a color column and no owner column.
          log.info("Porting players and colors from 0.1.0 style database with only a color columns in WORLDS");
          assignOwnersFromAncientColors = true;
          ResultSet rs = DbUtils.readQuery("SELECT DISTINCT color FROM WORLDS", null, connection);
          int i = 0;
          while(rs.next()) {
            String owner = "Player " + i++;
            String color = rs.getString("color");
            DbUtils.writeQuery("INSERT INTO ANCIENT_COLORS (owner, color) VALUES (?,?)",new Object[] { owner, color}, connection);
          }          
          //We assume the player with the nobody color is nobody
          DbUtils.writeQuery("UPDATE ANCIENT_COLORS SET owner=? WHERE color=?",new Object[] { IPlayer.NOBODY, nobodyColor}, connection);
        } else { 
          //Logic for 0.2.0 and beyond
          //Versions from 0.2.0 also had an owner column on worlds as well as color
          log.info("Porting players and colors from 0.2.0 - 0.4.x style database with both owner and color column in WORLDS");
          Set<String> owners = new HashSet<>(); //will use this to avoid duplicate player names
          ResultSet rs = DbUtils.readQuery("SELECT DISTINCT owner, color FROM WORLDS", null, connection);
          while(rs.next()) {
            String owner = rs.getString("owner");
            String color = rs.getString("color");
            owner = (owner==null) ? IPlayer.NOBODY : owner.trim();
            if(owner.isEmpty()) { owner = IPlayer.NOBODY; }
            color = (color==null) ? "" : color.trim();
            if(!owners.contains(owner)) { //lets just ignore dupes eh?
              log.info("Porting " + owner + " to ANCIENT_COLORS with color " + color);
              DbUtils.writeQuery("INSERT INTO ANCIENT_COLORS (owner, color) VALUES (?,?)",new Object[] { owner, color}, connection);
              owners.add(owner);
            }
          }
          //If we didn't import a Nobody player, then add one now
          if((Boolean)DbUtils.readSingleValue("SELECT COUNT(*)=0 FROM ANCIENT_COLORS WHERE owner=?", new Object[] { IPlayer.NOBODY }, connection) ) {
            log.info("Explicitly creating an entry for " + IPlayer.NOBODY + " in ANCIENT_COLORS as none was ported");
            DbUtils.writeQuery("INSERT INTO ANCIENT_COLORS (owner, color) VALUES (?,?)",new Object[] { IPlayer.NOBODY, nobodyColor}, connection);
          }          
        }         
      } //endif worldHasColorColumn && !playersTableExists   
      
      //Changes for 0.6.x
      //We need to detect if the db is an old or ancient one being updated and port its world information over into
      //the history table. Ideally we would use owner for this test, but earlier 0.6.x didn't remove it on the first startup.
      //Anyhow, if there is owner or fp info then we have something to import, otherwise there is nothing anyway.
      boolean worldsHistoryEmpty = !DbUtils.hasContent("WORLDS_HISTORY", connection);
      boolean worldsNotEmpty = DbUtils.hasContent("WORLDS",connection);
      boolean worldsHasFirepowerColumn = worldColumns.contains("FIREPOWER");
      boolean portHistory = worldsHistoryEmpty && worldsNotEmpty && (worldsHasOwnerColumn || worldsHasFirepowerColumn || worldHasColorColumn);
      log.debug("worldsHistoryEmpty=" + worldsHistoryEmpty + ", worldsNotEmpty=" + worldsNotEmpty
          + ", worldsHasOwnerColumn=" + worldsHasOwnerColumn + ", worldsHasFirepowerColumn=" + worldsHasFirepowerColumn
          + ", worldHasColorColumn=" + worldHasColorColumn + ". portHistory=" + portHistory);
      if(portHistory) {
        log.info("Porting history column values from WORLDS to WORLDS_HISTORY");
        
        //For the merge to work we need both these fields to exist, so ensure that's the case
        //We will delete them again later (hence the flags to keep track of whether we added them since
        //they arent in the column names set if they didnt exist)
        if(!worldColumns.contains("OWNER")) {
          DbUtils.writeQuery("ALTER TABLE WORLDS ADD COLUMN owner VARCHAR DEFAULT '"+IPlayer.NOBODY+"' NOT NULL", null, connection);
          addedOwner = true;
        } else {
          //Nulls in owner or fp would mess up the merge
          DbUtils.makeNonNull("WORLDS", "'"+IPlayer.NOBODY+"'", connection, "owner");
        }
        //Convert an empty owner to Nobody
        log.info("Converting empty WORLDS.owner values to " + IPlayer.NOBODY);
        DbUtils.writeQuery("UPDATE WORLDS SET owner=? WHERE TRIM(owner)=''",new Object[] {IPlayer.NOBODY}, connection);
        
        if(!worldColumns.contains("FIREPOWER")) {
          DbUtils.writeQuery("ALTER TABLE WORLDS ADD COLUMN firepower DOUBLE DEFAULT 0 NOT NULL", null, connection);
          addedFirepower = true;
        } else {
          DbUtils.makeNonNull("WORLDS", "0", connection, "firepower");
        }
        
        if(assignOwnersFromAncientColors) {
          //0.1.0 didn't have an owner field so we have to infer ownership from the colors
          //and then assign the worlds with those colors some placeholder player names we created
          log.info("Determining world ownership based on colors");
          ResultSet rs = DbUtils.readQuery("SELECT owner, color FROM ANCIENT_COLORS",null,connection);
          while(rs.next()) {
            String owner = rs.getString("owner");
            String color = rs.getString("color");
            log.info("Setting owner to " + owner + " where color is " + color);
            DbUtils.writeQuery("UPDATE WORLDS SET owner=? WHERE color=?",new Object[] { owner, color }, connection);
          }
        }
        
        //Copy exiting world data to the history table before updating the worlds
        //table schema to remove the now unwanted columns
        int assumedTurn = 1;
        String sql = "MERGE INTO WORLDS_HISTORY H USING (SELECT id,firepower,owner FROM WORLDS)"
            + " AS V(id,firepower,owner)" + " ON H.worldId=V.id"
            + " WHEN NOT MATCHED THEN INSERT (worldId,turn,firepower,owner,labour,capital)"
            + " VALUES (V.id, ?, V.firepower,V.owner,0,0)";
        DbUtils.writeQuery(sql, new Object[] { assumedTurn }, connection);
      } //endif portHistory
      
      if(addedOwner || worldColumns.contains("OWNER")) {
        //WORLDS.OWNER was introduced in 0.1.0 and used until 0.6.0 (before the history table was added)
        //Versions prior to 0.6.3 didn't remove it properly though.
        log.info("Dropping owner column from WORLDS table");
        DbUtils.writeQuery("ALTER TABLE WORLDS DROP COLUMN owner", null, connection);
      }
      if(addedFirepower || worldColumns.contains("FIREPOWER")) {
        //WORLDS.FIREPOWER was introduced in 0.5.? and used until the hitory table was introduced in 0.6.0
        //Versions prior to 0.6.3 didn't remove it properly though.
        log.info("Dropping firepower column from WORLDS table");
        DbUtils.writeQuery("ALTER TABLE WORLDS DROP COLUMN firepower", null, connection);
      }
      if(worldColumns.contains("COLOR")) {
        //WORLDS.COLOR was in the original 0.1.0 schema, and while 0.5.? introduced a color column in PLAYERS
        //it was still used for the actual world color. It was removed in 0.6.0 in favour of always using the
        //PLAYERS.COLOR directly.
        log.info("Dropping color column from WORLDS table");
        DbUtils.writeQuery("ALTER TABLE WORLDS DROP COLUMN color", null, connection);
      }
      if(!worldColumns.contains("SCANNERID")) {
        //WORLDS.SCANNERID was introduced in 0.6.0 and replaced WORLDS.SCANNER with a unit id instead of a range
        log.info("Adding scannerId column to WORLDS table");
        DbUtils.writeQuery("ALTER TABLE WORLDS ADD COLUMN scannerId VARCHAR", null, connection); //nb: this may still be null
        copyScanners(connection); //Convert the existing scanner data to use unit id 
      }
      if(worldColumns.contains("SCANNER")) {
        //WORLDS.SCANNER was introduced in 0.2.0 and used until 0.6.0. It held a scanner range rather than a unit id.
        String sql = "ALTER TABLE WORLDS DROP COLUMN scanner";
        log.info("Dropping scanner column from WORLDS table");
        DbUtils.writeQuery(sql, null, connection);
      }
      
      //Since 0.6.3 we don't want these columns to have nulls anymore (nb: scannerId remains nullable)
      if(nullableWorldColumns.contains("WORLDX")) {
        log.info("Making worldX,worldY,worldEi,worldRer columns in WORLDS table not null");
        DbUtils.makeNonNull("WORLDS", "0", connection, "worldX","worldY","worldEi","worldRer");
      }
      if(nullableWorldColumns.contains("NAME")) {
        log.info("Making name column in WORLDS table not null");
        DbUtils.makeNonNull("WORLDS", "''", connection, "name");
      }
      if(nullableWorldColumns.contains("NOTES")) {
        log.info("Making notes column in WORLDS table not null");
        DbUtils.makeNonNull("WORLDS", "''", connection, "notes");
      }
      
      if(!worldColumns.contains("NAMELOOKUP")) {
        //We also add a unique constrained uppercase only copy of the name column to enforce uniqueness of names
        //While we call this 'nameLookup' its really just there to enforce uniqueness for now.
        //I tried using VARCHAR_IGNORECASE in name column but couldn't get it to work
        //We also have to create and then alter the column with three separate statements as trying to do it all in one
        //go (like in the create statement) doesn't work here.
        log.info("Creating unique uppercase nameLookup column");
        DbUtils.writeQuery(
            "ALTER TABLE WORLDS ADD COLUMN nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE", null, connection);
//        DbUtils.writeQuery("ALTER TABLE WORLDS ALTER COLUMN nameLookup SET DEFAULT ''", null, connection);
//        DbUtils.writeQuery("ALTER TABLE WORLDS ALTER COLUMN nameLookup SET NOT NULL", null, connection);
      }
      
       
    } catch(Exception e) {
      throw new RuntimeException("Error updating columns in WORLDS", e);
    }
  }
  
  private void updateWorldsHistoryColumns(Connection connection, Set<String> historyColumns, Set<String> nullableHistoryColumns) {
    Log log = LogFactory.getLog(this.getClass());
    try {
      //As of 0.6.3 the owner column is now not null
      //Note that while this is used to refer to the name of the owner, and matched up with it in the
      //PLAYERS table for various purposes, it is a free text field, and is not constrained only to those
      //values in PLAYERS.name
      if(nullableHistoryColumns.contains("OWNER")) {
        log.info("Making column OWNER NOT NULL");
        DbUtils.makeNonNull("WORLDS_HISTORY", "''", connection, "owner"); 
      }
    } catch(Exception e) {
      throw new RuntimeException("Error updating columns in WORLDS_HISTORY", e);
    }
  }

  /**
   * Initialise the scannerId values for the worlds based on the scanner column
   * in the existing worlds
   */
  protected void copyScanners(Connection connection) throws SQLException {
    Log log = LogFactory.getLog(this.getClass());
    long worlds = (Long)DbUtils.readSingleValue("SELECT COUNT(*) FROM WORLDS WHERE scanner>0", null, connection);
    if(worlds > 0) { //If we have any existing worlds in the old format with a scanner range set then we shall get a list
                       //of the scanner units and initialise the scannerId field in worlds based
                     //on each of the existing scanners. Note that if more than one scanner type
                     //has the same range then we will use the first (by name) scanner in the list
                     //for those worlds. (We set the old scanner field to -1 so that we dont repeat
                     //the update query for future encounters with that range in another scanner type)
      log.info("Updating scanner data");
      
      //We don't want to create a unit for the default scanner range, so first we will convert those worlds with
      //that range
      long defaultRange = _settingsService.getLong(ISettings.DEFAULT_SCANNER);
      int count = DbUtils.writeQuery(
          "UPDATE WORLDS SET scannerId=null, scanner=-1 WHERE scanner=?",new Object[] { defaultRange }, connection);
      if(count>0) {
        log.info("Converted default range to null scannerId for " + count + " rows in WORLDS");
      } else {
        log.debug("No rows were found in WORLDS with default scanner range to be converted");
      }
      
      
      //Create a unit for any range that doesn't have a corresponding unit.
      ResultSet rangesRs = DbUtils.readQuery("SELECT DISTINCT scanner FROM WORLDS WHERE scanner<>-1",null,connection);
      while(rangesRs.next()) {
        int range = rangesRs.getInt(1);
        boolean unitExists = (Boolean)DbUtils.readSingleValue(
            "SELECT COUNT(*)>0 FROM UNITS WHERE scanner=?", new Object[] { range }, connection);
        if(!unitExists) {
          log.info("Creating implied scanner unit with range " + range);          
          String name = "Scanner " + range;
          String acronym = "S" + range;          
          DbUtils.writeQuery(
              "INSERT INTO UNITS (id, name, acronym, scanner) VALUES (?,?,?,?)",
              new Object[] { _keyGen.createIdKey(), name, acronym, range}, connection);
        }        
      }
      
      //nb: scannerId is the new column and refers to the unit.id of the scanner while
      //scanner is the old column and takes a scanner range. 
      PreparedStatement update = connection
          .prepareStatement("UPDATE WORLDS SET scannerId=?, scanner=-1 WHERE scanner=?");
      //Unported scanners will have a value in scanner (for ported ones we set it to -1)
      ResultSet scanners = DbUtils.readQuery("SELECT id,scanner FROM UNITS WHERE scanner>0 ORDER BY name ASC", null,
          connection);
      int scannerCount = 0;
      //We loop the scanner units and convert worlds that have a matching scanner range to use this unit
      while(scanners.next()) {
        int scannerRange = scanners.getInt("scanner");
        String scannerId = scanners.getString("id");
        log.info("Using unitId " + scannerId + " for scanner of worlds with range " + scannerRange);
        update.setString(1, scannerId);
        update.setInt(2, scannerRange);
        update.execute();
        scannerCount++;
      } //end while
      
      //Any worlds that didnt get ported will still have a proper value in scanner column
      //Log if any worlds were not ported log this as an error, but otherwise don't worry about it
      //(Shouldn't be possible since we created units for them!)
      long unported = (Long)DbUtils.readSingleValue("SELECT COUNT(*) FROM WORLDS WHERE scanner<>-1", null, connection);
      if(unported > 0) {
        log.error("Unable to port scanner for " + unported + " worlds due to lack of compatible scanner units. ("
            + scannerCount + " scanner units were available in the database).");
      }
    } //end if >0
  }

//  /**
//   * We now have an explicit constraint that world names cannot be duplicates. This will change the name of duplicated names
//   * in the old database so that the new constraint can be applied.
//   * @param connection
//   */
//  private void fixDuplicateNames(Connection connection) {
//    Log log = LogFactory.getLog(this.getClass());
//    //First we explicitly make sure all the existing name values in the old data are trimmed
//    if( (Boolean)DbUtils.readSingleValue("SELECT COUNT(*)>0 FROM worlds WHERE name <> TRIM(name)",null,connection)) {
//      log.info("Found untrimmed names in WORLDS. Trimming names.");
//      DbUtils.writeQuery("UPDATE worlds SET name=TRIM(name)",null,connection);
//    }
//    try {
//      //Now we will find the duplicate names and 'fix' them by changing the names of all but the first.
//      //Players would have to do further resolution manually, we won't try to merge the information.
//      //(An alternative would just  be to delete the duplicates under the assumption they are invalid copies)
//      ResultSet ucaseDupeNamesRs = DbUtils.readQuery(
//          "SELECT name FROM (SELECT UCASE(name) AS name FROM WORLDS) GROUP BY name HAVING COUNT(name)>1", null,
//          connection);
//      while(ucaseDupeNamesRs.next()) {
//        String name = ucaseDupeNamesRs.getString("name");
//        ResultSet dupeInstancesRs = DbUtils.readQuery(
//            "SELECT id,name FROM WORLDS WHERE UCASE(name)=? ORDER BY id",new Object[] { name }, connection);
//        dupeInstancesRs.next(); //Leave the first instance untouched
//        int d=2;
//        while(dupeInstancesRs.next()) {
//          WorldId id = new WorldId( dupeInstancesRs.getString("id") );
//          String newName = dupeInstancesRs.getString("name") + " #" + d++;
//          log.info("Resolving duplicate name conflict for world " + id + " by setting name to " + newName);
//          DbUtils.writeQuery("UPDATE WORLDS SET name=? WHERE id=?",new Object[] { newName, id }, connection);
//        }
//      }      
//    } catch(Exception e) {
//      throw new RuntimeException("Error fixing duplicate world names", e);
//    }
//  }
  
  /**
   * Delete all data in the WORLDS and WORLDS_HISTORY table
   * @param connection
   */
  @Override
  public void reset(Connection connection) {
    LogFactory.getLog(this.getClass()).debug("Deleting contents of the WORLDS and WORLDS_HISTORY table");
    DbUtils.writeQuery("DELETE FROM WORLDS;", null, connection);
    DbUtils.writeQuery("DELETE FROM WORLDS_HISTORY;", null, connection);
  }

}
