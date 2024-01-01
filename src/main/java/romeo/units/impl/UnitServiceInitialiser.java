package romeo.units.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import romeo.importdata.IUnitFile;
import romeo.importdata.IUnitImportReport;
import romeo.importdata.IUnitImporter;
import romeo.importdata.impl.AdjustmentsFileReader;
import romeo.importdata.impl.CsvUnitFile;
import romeo.model.api.IServiceInitialiser;
import romeo.units.api.Acronym;
import romeo.units.api.UnitId;
import romeo.utils.ClassPathFile;
import romeo.utils.Convert;
import romeo.utils.DbUtils;

/**
 * Initialise the Romeo database unit information. This includes invoking the
 * logic to read default units from unit.csv.
 */
public class UnitServiceInitialiser implements IServiceInitialiser {
  /**
   * Path to the csv in the resources folder (classpath relative)
   */
  public static final String UNITS_FILE_RESOURCE_PATH = "unit.csv";

  private final Logger _log;
  
  private AdjustmentsFileReader _adjustmentsReader;
  private List<String> _columnNames;
  private IUnitImporter _importer;
  
  /**
   * Constructor. Will not import any default units and adjustments.
   */
  public UnitServiceInitialiser(Logger log) {    
    this(log, null, null, null);
  }
  
  /**
   * Constructor
   * @param importer initial unit importer 
   * @param columnNames maps columns in the csv file to unit properties
   * @param reader default adjustments reader
   */
  public UnitServiceInitialiser(
      final Logger log, 
      final IUnitImporter importer, 
      final List<String> columnNames, 
      final AdjustmentsFileReader reader) {
    _log = Objects.requireNonNull(log, "log may not be null");
    _importer = importer;
    _columnNames = columnNames;
    _adjustmentsReader = reader;
    if(importer != null) {
      Objects.requireNonNull(columnNames, "columnNames may not be null if a unit importer is being used");
    }
  }

  /**
   * Initialise the units table
   * @param tableNames
   * @param connection
   */
  @Override
  public void init(Set<String> tableNames, Connection connection) {
    boolean createUnits = false;
    Set<String> columnNames;
    if(!tableNames.contains("UNITS")) {
      final String sql = "CREATE TABLE UNITS (" 
          + "id VARCHAR NOT NULL PRIMARY KEY" 
          + ",name VARCHAR DEFAULT '' NOT NULL" 
          + ",nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE"
          + ",attacks INTEGER DEFAULT 0 NOT NULL"
          + ",offense INTEGER DEFAULT 0 NOT NULL" 
          + ",defense INTEGER DEFAULT 0 NOT NULL" 
          + ",pd INTEGER DEFAULT 0 NOT NULL" 
          + ",speed INTEGER DEFAULT 0 NOT NULL" 
          + ",carry INTEGER DEFAULT 0 NOT NULL"
          + ",cost INTEGER DEFAULT 0 NOT NULL" 
          + ",complexity INTEGER DEFAULT 0 NOT NULL" 
          + ",scanner INTEGER DEFAULT 0 NOT NULL" 
          + ",license INTEGER DEFAULT 0 NOT NULL" 
          + ",acronym VARCHAR DEFAULT '' NOT NULL"
          + ",xFactor VARCHAR" //nb: xfactor stays nullable as one day it will be a FK
          + ");";

      _log.info("Creating UNITS table");
      DbUtils.writeQuery(sql, null, connection);
      //If an importer was provided we will want to import initial unit data
      //Note that this only happpens for a new install (ie: if we had to create the units table here now)
      createUnits = (_importer != null); 
      columnNames = DbUtils.getColumnNames(connection, "UNITS");
    } else {
      columnNames = DbUtils.getColumnNames(connection, "UNITS");
      if(DbUtils.fixEmptyColumn("UNITS", "name", "Untitled", connection)) {
        _log.info("Changed empty names to 'Untitled'");
      }
      
      //Ensure all the names are trimmed and unique. (We found that UC actually has some nobody world names with extra whitespace and that
      //it allows this for player homeworld names too)
      if( DbUtils.ensureTrimmed("UNITS", "name", connection) ) {
        _log.info("Trimmed whitespace from names");
      }
      if( DbUtils.fixDuplicates("UNITS", "name", connection) ) {
        _log.info("Fixed duplicate values in names");
      }
    }
    
    Set<String> nullableColumns = DbUtils.getNullableColumnNames(connection, "UNITS");
    _log.debug("Existing columns in UNITS table=" + columnNames);
    updateColumns(connection, columnNames, nullableColumns);

    if(createUnits) {
      createDefaultUnits(connection);
    }
  }

  private void updateColumns(Connection connection, Set<String> columnNames, Set<String> nullableColumns) {
    boolean addedAcronym = false;
    if(!columnNames.contains("ACRONYM")) {
      _log.info("Adding acronym column to UNITS table");
      DbUtils.writeQuery("ALTER TABLE UNITS ADD COLUMN acronym VARCHAR DEFAULT '' NOT NULL", null, connection);
      addedAcronym = true;
      
      //Import the acronym values from the adjustments file for existing units in the upgraded database
      applyDefaultAdjustmentFor(connection, "acronym");
    } 

    if(!columnNames.contains("XFACTOR")) {
      _log.info("Adding xfactor column to UNITS table");
      DbUtils.writeQuery("ALTER TABLE UNITS ADD COLUMN xfactor VARCHAR", null, connection); //nb: may be null
      //nb: we can't import xFactors for existing units in an old database here because we haven't run the
      //    XFactors service initialiser yet, and will need to convert the xf name in the adjustments into
      //    an actual xf id. The XFactorServiceInitialiser already has logic to link the XFactors to the
      //    units on its initial import of the defaul XFactors, and that import will be happening in this
      //    case - so old units will pick up the xFactor at that point IF they have an acronym set.
      
      /**
       * Its worth noting that while this is a foreign key to the id in the
       * XFactors table we dont yet do any joins on it (its all programatic)
       * and so no point informing the database about the relationship. (We
       * might in a future version, but right now it will probably just waste
       * our time and its time also)
       */
    }
    
    //As of 0.6.3 we don't allow null values in units columns other than the optional xfactor 
    //(at some point xfactor may become an actual foreign key so will need to stay nullable)
    if(addedAcronym || nullableColumns.contains("ACRONYM")) {
      _log.debug("Making acronym and name columns NOT NULL");
      DbUtils.makeNonNull("UNITS", "''", connection, 
          "acronym", "name");
    }
    if(nullableColumns.contains("ATTACKS")) {
      _log.debug("Making stats columns NOT NULL");
      DbUtils.makeNonNull("UNITS", "0", connection, 
          "attacks","offense","defense","pd","speed","carry","cost","complexity","scanner","license");
    }
    
    if(!columnNames.contains("NAMELOOKUP")) {
      //Like the similar column over in worlds, this is mainly just here to enforce unique names
      //in a case-insensitive manner
      _log.info("Creating unique uppercase nameLookup column");
      DbUtils.writeQuery(
          "ALTER TABLE UNITS ADD COLUMN nameLookup VARCHAR GENERATED ALWAYS AS (UCASE(name)) UNIQUE", null, connection);
//      DbUtils.writeQuery("ALTER TABLE PLAYERS ALTER COLUMN nameLookup SET DEFAULT ''", null, connection);
//      DbUtils.writeQuery("ALTER TABLE PLAYERS ALTER COLUMN nameLookup SET NOT NULL", null, connection);
    }
    
    if(!columnNames.contains("ACRONYMLOOKUP")) {
      if(_log.isTraceEnabled()) {
        _log.trace("existing acronym values:" + DbUtils.readRow("UNITS", "acronym", connection));
      }
      //As of 0.6.3 acronyms are required and may not be unique. We need to cleanup existing blanks and whitespace
      //in the acronym column and provide a placeholder acronym for any unit that doesnt have one yet.
      if(DbUtils.ensureTrimmed("UNITS", "acronym", connection)) {
        _log.info("Trimmed whitespace from acronym");
      }
      provideAcronyms(connection);
      if(DbUtils.fixDuplicates("UNITS", "acronym", connection)) {
        _log.info("Fixed duplicate values in acronym");
      }     
      //This will enforce case-insensitive uniqueness of acronyms
      _log.info("Creating unique uppercase acronymLookup column");
      DbUtils.writeQuery(
          "ALTER TABLE UNITS ADD COLUMN acronymLookup VARCHAR GENERATED ALWAYS AS (UCASE(acronym)) UNIQUE", null, connection);
    }
  }


  /**
   * Delate all the units from the database and if an importer was provided, re-import defaults units.
   * @param connection
   */
  @Override
  public void reset(Connection connection) {
    DbUtils.writeQuery("DELETE FROM UNITS;", null, connection);
    if(_importer != null) {
      createDefaultUnits(connection);
    }
  }

  /**
   * Reads the default units data from DEFAULT_UNITS_PATH
   * @param connection
   */
  private void createDefaultUnits(Connection connection) {

    Objects.requireNonNull(_importer, "_importer may not be null here");
    
    _log.info("Inserting default unit data into the database");

    IUnitFile unitFile = null;

    try {
      ClassPathFile defUnitsCsv = new ClassPathFile(UNITS_FILE_RESOURCE_PATH);
      InputStream stream = defUnitsCsv.getInputStream();
      _log.debug("CSV Columns=" + _columnNames);
      final String nameColumn = "name"; //No longer support setting this in context
      unitFile = new CsvUnitFile(LoggerFactory.getLogger(CsvUnitFile.class), stream, Convert.toStrArray(_columnNames), nameColumn);
    } catch(Exception e) {
      throw new RuntimeException("Error reading default unit data file", e);
    }

    try {
      Map<String, Map<String, String>> adjustmentsMap;
      if(_adjustmentsReader != null ) {
        adjustmentsMap = _adjustmentsReader.read();
        _log.info("Using adjustment map containing " + adjustmentsMap.size() + " definitions");
      } else {
        _log.debug("No adjustments reader provided");
        adjustmentsMap = null;
      }
      
      IUnitImportReport report = _importer.importData(unitFile, adjustmentsMap, false); //the initial import of units
      _log.info("Imported default data for " + report.getImportedUnitsCount() + " units");
      if(report.getException() != null) {
        throw report.getException();
      }
    } catch(Exception e) {
      throw new RuntimeException("Failed to correctly import default unit data!", e);
    }
  }
  
  /**
   * Used when upgrading an ancient units table. Will apply only the specified adjustment property read
   * from the adjustments file. Calling this method for multiple separate adjustments is somewhat inefficient
   * so try to restrict its use to one-off situations such as updating old databases. Try not to use it with
   * new installations or on every startup!
   * nb: by default will use sql type conversions to set the property directly to the table. 
   * WARNING: the property will be used directly as a column name in the sql, be sure it is safe!
   * @param connection
   * @param property name of the property to copy from the adjustments map
   */
  public void applyDefaultAdjustmentFor(Connection connection, String property) {
    if(_adjustmentsReader==null) {
      _log.debug("Unable to apply default adjustments for specific property " + property + " as no adjustments reader provided");
      return;
    }
    Map<String, UnitId> unitIds = getUnitSignatureMap(connection);
    if(unitIds.isEmpty()) {
      return;
    }
    try {
      //The adjustments are a map of maps, keyed by the unit's name's md5 signature.
      //Each submap is a map of property name to value, but represented as a string
      //nb: X-factors aren't implemented as asjustments!
      Map<String,Map<String,String>> adjustments = _adjustmentsReader.read();
      for(String signature : adjustments.keySet()) {
        UnitId id = unitIds.get(signature);
        if(id!=null) {
          String value = adjustments.get(signature).get(property);
          if(value != null) {
            _log.info("Applying specific " + property + " adjustment to unit " + id + " with value " + value);
            DbUtils.writeQuery("UPDATE UNITS SET " + property + "=? WHERE id=?", new Object[] { value, id }, connection);
          }
        }
      }
    } catch(Exception e) {
      throw new RuntimeException("Error applying default adjustment for specific property:" + property,e);
    }
  }
  
  /**
   * Returns a map of unit signatures (md5 of the name) to their id. In the case of duplicate names
   * only one would be returned.
   * @param connection
   * @return map
   */
  private Map<String, UnitId> getUnitSignatureMap(Connection connection) {
    Map<String,UnitId> map = new HashMap<>();
    try{
      ResultSet rs = DbUtils.readQuery("SELECT name, id FROM UNITS", null, connection);
      while(rs.next()) {
        String name = rs.getString("name");
        String signature = Convert.toDigestSignature(name.getBytes());
        String id = rs.getString("id");
        map.put(signature, new UnitId(id) );        
      }
      return map;
    } catch(Exception e) {
      throw new RuntimeException("Error getting unit signature to id map",e);
    }
  }
  
  /**
   * Generates placeholder acronyms for existing units in the database that haver an empty string or null for their
   * acronym.
   * @param connection
   * @throws SQLException
   */
  private void provideAcronyms(Connection connection) {
    try {
      PreparedStatement ps = connection.prepareStatement("UPDATE UNITS SET acronym=? WHERE id=?");
      ResultSet rs = DbUtils.readQuery("SELECT id,name FROM UNITS WHERE acronym='' OR acronym IS NULL", null, connection);
      while(rs.next()) {
        Object id = rs.getObject(1);
        String name = rs.getString(2);
        Acronym acronym = UnitImpl.generatePlaceholderAcronym(name);
        ps.setString(1,acronym.toString());
        ps.setObject(2,id);
        ps.execute();
        if(ps.getUpdateCount()>0) {
          _log.info("Created placeholder acronym \"" + acronym + "\" for existing unit " + id);
        } else {
          throw new RuntimeException("Failed to update acronym for unit " + id);
        }
      } 
    }catch(SQLException e) {
      throw new RuntimeException("Error providing placeholder acronyms for existing units", e);
    }
  }

}








