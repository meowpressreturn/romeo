package romeo.xfactors.impl;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.importdata.impl.XFactorFileReader;
import romeo.model.api.IServiceInitialiser;
import romeo.units.api.IUnitService;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;

/**
 * Initialise the XFACTORS table in the db if necessary. This includes invoking
 * the logic that reads the defaultXfactors.xml file and links these to the
 * appropriate units. This initialiser should be run AFTER the units
 * initialiser.
 */
public class XFactorServiceInitialiser implements IServiceInitialiser {
  private IKeyGen _keyGen;
  private XFactorFileReader _xfReader;
  private IUnitService _unitService;
  
  /**
   * Constructor. The reader is optional, but the keyGen is required.
   * @param reader
   * @param keyGen
   */
  public XFactorServiceInitialiser(IUnitService unitService, XFactorFileReader reader, IKeyGen keyGen) {
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _xfReader = reader;
    _keyGen = Objects.requireNonNull(keyGen, "keyGen may not be null");
  }

  /**
   * Create the XFACTORS table if it doesnt exist
   * @param tableNames
   * @param connection
   */
  @Override
  public void init(Set<String> tableNames, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    if(!tableNames.contains("XFACTORS")) {
      String sql = "CREATE TABLE XFACTORS (" 
          + "id VARCHAR NOT NULL PRIMARY KEY" 
          + ",name VARCHAR DEFAULT '' NOT NULL"
          + ",nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE"
          + ",description VARCHAR DEFAULT '' NOT NULL" 
          + ",xfTrigger VARCHAR DEFAULT '' NOT NULL" //Had to be renamed as hsqldb2 doesnt allow TRIGGER
          + ",xfAttacks VARCHAR DEFAULT '' NOT NULL" 
          + ",xfOffense VARCHAR DEFAULT '' NOT NULL" 
          + ",xfDefense VARCHAR DEFAULT '' NOT NULL" 
          + ",xfPd VARCHAR DEFAULT '' NOT NULL" 
          + ",xfRemove VARCHAR DEFAULT '' NOT NULL"
          + ");";
      log.info("Creating XFACTORS table");
      DbUtils.writeQuery(sql, null, connection);
      createDefaultXFactors(connection);
      _unitService.dataChangedExternally();
    }  else {
      if(DbUtils.fixEmptyColumn("XFACTORS", "name", "Untitled", connection)) {
        log.info("Changed empty names to 'Untitled'");
      }
      //Ensure all the names are trimmed and unique. (We found that UC actually has some nobody world names with extra whitespace and that
      //it allows this for player homeworld names too)
      if( DbUtils.ensureTrimmed("XFACTORS", "name", connection) ) {
        log.info("Trimmed whitespace from names");
      }
      if( DbUtils.fixDuplicates("XFACTORS", "name", connection) ) {
        log.info("Fixed duplicate values in names");
      }
    }

    Set<String> columnNames = DbUtils.getColumnNames(connection, "XFACTORS" );
    log.debug("Existing columns in XFACTORS table=" + columnNames);
    Set<String> nullableColumns = DbUtils.getNullableColumnNames(connection, "XFACTORS") ;
    log.debug("Nullable columns in XFACTORS table=" + nullableColumns);
    createColumns(connection, columnNames, nullableColumns);
  }

  private void createColumns(Connection connection, Set<String> columnNames, Set<String> nullableColumns) {
    Log log = LogFactory.getLog(this.getClass());
    //nb: when creating a new database, the xfpd and xfremove columns would have already been added so that the
    //default xfactors could be imported. The below changes would only be applied to an old db being updated.
    try {
      if(!columnNames.contains("XFPD")) {
        log.info("Adding xfPd column to XFACTORS table");
        DbUtils.writeQuery("ALTER TABLE XFACTORS ADD COLUMN xfPd VARCHAR DEFAULT '' NOT NULL", null, connection);
      }

      if(!columnNames.contains("XFREMOVE")) {
        log.info("Adding xfRemove column to XFACTORS table");
        DbUtils.writeQuery("ALTER TABLE XFACTORS ADD COLUMN xfRemove VARCHAR DEFAULT '' NOT NULL", null, connection);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error initialising extra columns", e);
    }
    
    //As of 0.6.3 all the columns in XFACTORS must be not nullable. If the description is still nullable we assume
    //this hasn't been done yet
    if(nullableColumns.contains("DESCRIPTION")) {
      log.info("Making columns in XFACTORS table NOT NULL");
      DbUtils.makeNonNull("XFACTORS", "''", connection, 
          "name","description","xfTrigger","xfAttacks","xfOffense","xfDefense","xfPd","xfRemove");
    }
    
    if(!columnNames.contains("NAMELOOKUP")) {
      //Like the similar column over in worlds, this is mainly just here to enforce unique names
      //in a case-insensitive manner
      log.info("Creating unique uppercase nameLookup column");
      DbUtils.writeQuery(
          "ALTER TABLE XFACTORS ADD COLUMN nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE", null, connection);
//      DbUtils.writeQuery("ALTER TABLE PLAYERS ALTER COLUMN nameLookup SET DEFAULT ''", null, connection);
//      DbUtils.writeQuery("ALTER TABLE PLAYERS ALTER COLUMN nameLookup SET NOT NULL", null, connection);
    }
  }

  /**
   * Delete all the xfactors in the table
   * @param connection
   */
  @Override
  public void reset(Connection connection) {
    LogFactory.getLog(this.getClass()).debug("Deleting data in XFACTORS table");
    DbUtils.writeQuery("DELETE FROM XFACTORS;", null, connection);
  }

  /**
   * Creates the default xfactor definitions. These come from the
   * {@link XFactorFileReader}.
   * @param connection
   */
  private void createDefaultXFactors(Connection connection) {
    try {
      Log log = LogFactory.getLog(this.getClass());
      

      if(_xfReader != null) {
        log.info("Inserting default X-Factor definitions into database");
        List<Map<String, Object>> unitMaps = _xfReader.read();
        for(Map<String, Object> xfMap : unitMaps) {
          String id = _keyGen.createIdKey();
          String name = (String)xfMap.get("name");

          log.info("Inserting default X-Factor definition \"" + name + "\" with id " + id);

          final String sql = "INSERT INTO XFACTORS (id,name,description,xfTrigger,xfAttacks,xfOffense,xfDefense,xfPd,xfRemove) "
              + "VALUES (?,?,?,?,?,?,?,?,?);";
          Object[] parameters = new Object[] { id, name, xfMap.get("description"), xfMap.get("trigger"),
              xfMap.get("attacks"), xfMap.get("offense"), xfMap.get("defense"), xfMap.get("remove"), xfMap.get("pd"), };
          DbUtils.writeQuery(sql.toString(), parameters, connection);

          @SuppressWarnings("unchecked")
          List<String> linkedUnits = (List<String>) xfMap.get("link");
          for(String acronym : linkedUnits) {
            linkNewRemoveOld(acronym, id, connection);
          }

        }
      } else {
        log.info("Not inserting default X-Factor definitions into database - no reader supplied");
      }
    } catch(Exception e) {
      throw new RuntimeException("Exception preparing and linking default xfactors", e);
    }
  }

  /**
   * Removes the old xfactor and links the new one for the specified unit. This
   * is used when updating from an earlier Romeo version 
   * @param acronym
   * @param xfId
   * @param connection
   */
  protected void linkNewRemoveOld(String acronym, String xfId, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    try {
      log.info("Linking " + acronym + " (if present) to xFactor definition " + xfId);
      Object[] parameters = new Object[] { xfId, acronym.toUpperCase(Locale.US) };
      DbUtils.writeQuery("UPDATE UNITS SET xfactor=? WHERE UCASE(acronym)=?;", parameters, connection);
    } catch(Exception e) {
      throw new RuntimeException("Exception occured updating X-Factor for:" + acronym, e);
    }
  }

}
