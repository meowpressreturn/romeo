package romeo.importdata.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.importdata.IUnitFile;
import romeo.importdata.IUnitImportReport;
import romeo.importdata.IUnitImporter;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;
import romeo.units.impl.UnitImpl;
import romeo.utils.Convert;

/**
 * Implementation of the unit import logic. This implementation also allows for
 * an adjustments map (which would be read using an AdjustmentsFileReader) to
 * allow for properties of imported units to be overridden. The signatures used
 * in the map can be either the unit name (case sensitive) or the md5 hash of
 * the name.
 */
public class UnitImporterImpl implements IUnitImporter {

  private IUnitService _unitService;

  /**
   * Constructor
   * @param unitService
   * @param enableUpdate Sets whether update of existing units is allowed (Initial unit import would have this set to false)
   * @param excludeNonComs
   */
  public UnitImporterImpl(IUnitService unitService) {
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
  }


  /**
   * Import unit data from the unitFile into Romeo.
   * The order in which units are added to the database (and thus the id assigned by the service) is not guaranteed.
   * @param unitFile
   */
  @Override
  public IUnitImportReport importData(IUnitFile unitFile, Map<String, Map<String, String>> adjustments, boolean enableUpdate) {
    Objects.requireNonNull(unitFile, "unitFile may not be null");
    if(adjustments==null) {
      adjustments = Collections.emptyMap();
    }
    UnitImportReportImpl report = new UnitImportReportImpl();
    try {
      String nameColumn = unitFile.getNameColumn();
      boolean enableImport = true;
      Iterator<Map<String, String>> unitFileIterator = unitFile.iterator();
      Log log = LogFactory.getLog(this.getClass());
      //Build a lookup table of units keyed by name
      List<IUnit> existingUnits = _unitService.getUnits();
      Map<String, IUnit> unitLookup = new TreeMap<String, IUnit>();
      for(IUnit unit : existingUnits) {
        String key = unit.getName().toUpperCase(Locale.US);
        unitLookup.put(key, unit);
      }
      List<IUnit> updateUnits = new ArrayList<>(existingUnits.size()); //will hold the updated unit objects
      List<IUnit> importUnits = new ArrayList<>(existingUnits.size()); //will hold the newly imported unit objects
      //Now perform the importing
      while(unitFileIterator.hasNext()) {
        Map<String, String> unitData = unitFileIterator.next();
        log.debug("Analysing data:" + unitData);
        String name = (String) unitData.get(nameColumn);
        try {
          String key = name.toUpperCase(Locale.US);
          IUnit unit = (IUnit) unitLookup.get(key);
          if(unit == null) {
            if(enableImport) {
              unit = createUnit(unitData, name, adjustments);
              log.debug("Importing new unit:" + unit.getName());
              importUnits.add(unit); 
            }
          } else {
            if(enableUpdate) {
              //If the unit is already in our db we will update it even if it is a
              //noncom or is about to become one.
              unit = updateUnit(unitData, unit); //writes changes to existing object
              unitLookup.put(unit.getName().toUpperCase(Locale.US), unit);
              updateUnits.add(unit);
              log.debug("Updating existing unit unit:" + unit.getName());
            }
          }
        } catch(Exception e) {
          throw new RuntimeException("Error interpreting data for " + name, e);
        }
      }
      //Finally save the units
      List<IUnit> units = new ArrayList<>(updateUnits.size() + importUnits.size());
      units.addAll(updateUnits);
      units.addAll(importUnits);
      log.info("Saving imported and updated unit data");
      List<UnitId> ids = _unitService.saveUnits(units);
      log.info("Changes saved for " + ids.size() + " records");
      
      
      //TODO having just modified the report to no longer hold the actual records imported, the logic
      //here is unnecessarily convoluted and could use refactoring
      List<UnitId> updatedIds = ids.subList(0, updateUnits.size());
      updateUnits = loadUnits(updatedIds); //Reload to get them as service now sees them
      //for(IUnit unit : updateUnits) { report.addUpdated(unit); }
      for(@SuppressWarnings("unused") IUnit unit : updateUnits) { report.addUpdated(); }
      
      List<UnitId> importedIds = (updateUnits.size() < units.size()) ? ids.subList( updateUnits.size(), units.size() ) : new ArrayList<UnitId>(0);
      importUnits = loadUnits(importedIds);
      //for(IUnit unit : importUnits) { report.addImported(unit); }
      for(@SuppressWarnings("unused")IUnit unit : importUnits) { report.addImported(); }
      
      
    } catch(Exception e) {
      Exception ex = new RuntimeException("Import Failure:" + e.getMessage(), e);
      report.setException(ex);
    }
    return report;
  }
  
  private List<IUnit> loadUnits(List<UnitId> ids) {
    List<IUnit> units = new ArrayList<>(ids.size());
    for(UnitId id : ids) {
      units.add( _unitService.getUnit(id) );
    }
    return units;
  }

  /**
   * Create a {@link Unit} based on data in the map. Information in the
   * adjustments map is taken into account.
   * @param unitData
   * @param name
   *          used in place of acronym if the adjustments did not specify one
   */
  private IUnit createUnit(Map<String, String> unitColumns, String name, Map<String, Map<String, String>> adjustmentsMap) {
    
    try {
      if(unitColumns.get("acronym") == null || "".equals(unitColumns.get("acronym"))) {
        String unitName = (String)unitColumns.get("name");
        Objects.requireNonNull("unit name may not be null. data=" + unitColumns);
        Map<String, String> adjustments = adjustmentsMap.get(unitName);
        if(adjustments == null) {
          String signature = Convert.toDigestSignature(unitName.getBytes());
          adjustments = adjustmentsMap.get(signature);
        }
        if(adjustments == null) {
          //For units without an adjustment we generate a placeholder acronym by removing spaces from its name
          unitColumns.put("acronym", UnitImpl.generatePlaceholderAcronym(name) );
        } else {
          copyAdjustmentsToUnit(adjustments, unitColumns);
        }
      }
      Map<String,Object> unit = extractUnitColumns(unitColumns);
      IUnit record = UnitImpl.createFromMap(unit);
      return record;
    } catch(Exception e) {
      throw new RuntimeException("Exception importing unit data:" + unitColumns, e);
    }
  }
  
  /**
   * Extract only those values we support in IUnit from the csv column data and convert data types.
   * @param data
   * @return unitData
   */
  private Map<String,Object> extractUnitColumns(Map<String,String> data) {
    Map<String,Object> unit = new HashMap<>();
    unit.put( "name", (String)data.get("name").trim() );
    unit.put( "attacks", Convert.toInt((String)data.get("attacks")) );
    unit.put( "offense", Convert.toInt((String)data.get("offense")) );
    unit.put( "defense", Convert.toInt((String)data.get("defense")) );
    unit.put( "pd", Convert.toInt((String)data.get("pd")) );
    unit.put( "speed", Convert.toInt((String)data.get("speed")) );
    unit.put( "carry", Convert.toInt((String)data.get("carry")) );
    unit.put( "cost", Convert.toInt((String)data.get("cost")) );
    unit.put( "complexity", Convert.toInt((String)data.get("complexity")) );
    unit.put( "scanner", Convert.toInt((String)data.get("scanner")) );
    unit.put( "license", Convert.toInt((String)data.get("license")) );
    unit.put( "acronym", (String)data.get("acronym") );
    return unit;    
  }

  /**
   * Updates an existing unit with values from the map (via a call to
   * copyDataToUnit). Does not refer to the adjustments.
   * @param unitData
   * @param unit
   *          unit to write to
   */
  private UnitImpl updateUnit(Map<String, String> unitColumns, IUnit oldUnit) {
    //nb: this method previously used copyDataToUnit() to extract the values
    Map<String,Object> unit = extractUnitColumns(unitColumns);
    //Need to preserve the old id and acronym as these will not be in the imported column values 
    unit.put("id", oldUnit.getId() );
    unit.put("acronym", oldUnit.getAcronym() );
    UnitImpl newUnit = UnitImpl.createFromMap(unit);
    return newUnit;   
  }

  /**
   * Copies adjustments to the map of unit column values
   * @param adjustmentData
   * @param unitColumns
   */
  private void copyAdjustmentsToUnit(Map<String, String> adjustmentData, Map<String,String> unitColumns) {
    try {
      Iterator<Map.Entry<String, String>> adjEntries = adjustmentData.entrySet().iterator();
      while(adjEntries.hasNext()) {
        Map.Entry<String, String> entry = adjEntries.next();
        String key = (String) entry.getKey();
        String value = (String) entry.getValue();
        if(value != null && !"signature".equals(key)) {
          //nb: previously BeanUtils was used to copy the value into a mutable UnitImpl property
          unitColumns.put(key, value);
        }
      }
    } catch(Exception e) {
      throw new RuntimeException("Exception copying property values from map to unit", e);
    }
  }
}
