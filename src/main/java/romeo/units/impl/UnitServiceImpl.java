package romeo.units.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.LogFactory;

import romeo.persistence.AbstractPersistenceService;
import romeo.persistence.DuplicateRecordException;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;
import romeo.units.api.UnitUtils;
import romeo.utils.BeanComparator;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.xfactors.api.XFactorId;

/**
 * Provides the underlying logic for persisting unit definitions. Other classes
 * should access this via the IUnitService interface which is its public API.
 */
public class UnitServiceImpl extends AbstractPersistenceService implements IUnitService {
  
  private List<IUnit> _unitsCache;
  private Map<UnitId, IUnit> _unitsById;
  private Map<String, IUnit> _unitsByName;
  private Map<String, IUnit> _unitsByAcronym;
  private int[] _speeds;
  private Map<String, double[]> _ranges; //no, not scanner, but min max of stats

  public UnitServiceImpl(DataSource dataSource, IKeyGen keyGen) {
    super(dataSource, keyGen);
  }

  /**
   * Get the range of values in the persisted data for a specified property
   * @param property
   *          name of a numeric property
   * @return range index 0 is min, and 1 the max
   */
  @Override
  public synchronized double[] getRange(String property) {
    Objects.requireNonNull(property, "property may not be null");
    if(property.isEmpty()) {
      throw new IllegalArgumentException("property may not be empty");
    }
    if(!rangeCacheInitialised()) {
      _ranges = new TreeMap<String, double[]>();
    }

    double[] ranges = (double[]) _ranges.get(property);
    if(ranges != null) {
      return ranges;
    }

    if("multipliedOffense".equals(property)) {
      Collection<IUnit> units = getUnits();
      ranges = UnitUtils.getMultipliedOffenseRange(units);
      _ranges.put("multipliedOffense", ranges);
      return ranges;
    }

    if("logisticsFactor".equals(property)) {
      Collection<IUnit> units = getUnits();
      ranges = UnitUtils.getLogisticsFactorRange(units);
      _ranges.put("logisticsFactor", ranges);
      return ranges;
    }

    //not in cache so work it out and cache it before returning
    Iterator<IUnit> i = getUnits().iterator();
    ranges = new double[2];
    if(i.hasNext()) {
      try {
        ranges[0] = Double.MAX_VALUE;
        ranges[1] = Double.MIN_VALUE;
        while(i.hasNext()) {
          IUnit unit = i.next();
          Object valueObject = PropertyUtils.getProperty(unit, property);
          double value = Convert.toDouble(valueObject);
          if(value < ranges[0]) {
            ranges[0] = value;
          }
          if(value > ranges[1]) {
            ranges[1] = value;
          }
        }
        return ranges;
      } catch(Exception e) {
        throw new RuntimeException("Exception getting ranges for unit property:" + property, e);
      }
    }
    _ranges.put(property, ranges);
    return ranges;
  }

  /**
   * Returns a List of unit information containing all the persisted units.
   * Cached data is used if available.
   * @return units
   */
  @Override
  public synchronized List<IUnit> getUnits() {
    if(!unitCacheInitialised()) {
      initUnitCache();
    }
    return Collections.unmodifiableList(_unitsCache);
  }

  /**
   * Saves or updates a unit's information in the database and notifies
   * listeners.
   * @param unit
   */
  @Override
  public synchronized UnitId saveUnit(IUnit unit) {
    Objects.requireNonNull(unit, "unit must not be null");
    checkDuplicate(unit);
    UnitId id = null;
    try(Connection connection = _dataSource.getConnection()) {
      id = saveUnitInternal(connection, unit);
    } catch(Exception e) {
      throw new RuntimeException("Error saving unit " + unit, e);
    } finally {
      flushAllCaches();
      notifyDataChanged();
    }
    return id;
  }
  
  @Override
  public synchronized void unlinkUnitsWithXFactor(XFactorId xFactorId) {
    Objects.requireNonNull(xFactorId, "xFactorId may not be null");
    try (Connection connection = _dataSource.getConnection() ) {
      final String sql = "UPDATE UNITS SET xfactor='' WHERE xfactor=?";
      LogFactory.getLog(this.getClass()).debug("Unlinking units with X-Factor " + xFactorId);
      DbUtils.writeQuery(sql, new Object[] { xFactorId }, connection);      
    } catch(Exception e) {
      throw new RuntimeException("Error unlinking units with X-Factor " + xFactorId,e);
    } finally {
      flushAllCaches();
      notifyDataChanged();
    }
  }

  /**
   * Saves or updates multiple unit records in the database and then notifies
   * listeners. The ids of the saved units are returned (in same indexes of course)
   * @param units
   * @return ids
   */
  @Override
  public synchronized List<UnitId> saveUnits(List<IUnit> units) {
    Objects.requireNonNull(units, "units must not be null");
    
    //Check that within the list of units being saved, none have the same name as another
    Set<String> names = new HashSet<>();
    Set<String> acronyms = new HashSet<>();
    for(IUnit unit : units) {
      Objects.requireNonNull( unit.getName(), "unit.name may not be null" );
      Objects.requireNonNull( unit.getAcronym(), "unit.acronym may not be null" );
      String name = unit.getName().trim().toUpperCase(Locale.US);
      if(names.contains(name)) {
        throw new DuplicateRecordException("Duplicated unit name in the save list: " + name);
      }
      String acronym = unit.getAcronym().trim().toUpperCase(Locale.US);
      if(acronyms.contains(acronym)) {
        throw new DuplicateRecordException("Duplicated unit acronym in the save list:" + acronym);
      }
      names.add(name);
      acronyms.add(acronym);
    }
    
    List<UnitId> ids = new ArrayList<>(units.size());
    if(!units.isEmpty()) {
      try(Connection connection = _dataSource.getConnection()) {
        for(IUnit unit : units) {
          checkDuplicate(unit);
          try {
            UnitId id = saveUnitInternal(connection, unit);
            ids.add(id);
          } catch(Exception e) {
            throw new RuntimeException("Error saving unit " + unit, e);
          }
        }
      } catch(DuplicateRecordException dre) {
        throw dre;
      } catch(Exception e) {
        throw new RuntimeException("Error saving units", e);
      } finally {
        flushAllCaches();
        notifyDataChanged();
      }
    }
    return ids;
  }

  /**
   * Returns information for the specified unit
   * @param id
   * @return unit
   */
  @Override
  public synchronized IUnit getUnit(UnitId id) {
    Objects.requireNonNull(id, "id may not be null");
    //We no longer load individual units from the db when the cache is uninitialised. Now we just 
    //return them from the cache, and initialise first is necessary.
    if(!unitCacheInitialised()) {
      getUnits();
    }
    return _unitsById.get(id);
  }

  /**
   * Removes a unit from the database and notifies listeners.
   * Nb: id may not be null or empty but this method doesn't verify that the
   * specified unit actually exists.
   * @param id
   */
  @Override
  public synchronized void deleteUnit(UnitId id) {
    Objects.requireNonNull(id, "id may not be null");
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "DELETE FROM UNITS WHERE id=?";
      DbUtils.writeQuery(sql, new Object[] { id }, connection);      
    } catch(Exception e) {
      throw new RuntimeException("Error deleting unit " + id, e);
    } finally {
      flushAllCaches();
      notifyDataChanged();
    }
  }

  /**
   * Returns an ordered array of unique unit speeds.
   * nb: as of 0.6.3 we return a copy of the cached speed data to avoid modification issues
   * @return speeds
   */
  @Override
  public synchronized int[] getSpeeds() {
    if(!speedCacheInitialised()) {
      initSpeedCache();
    }    
    return Arrays.copyOf(_speeds, _speeds.length);
  } 

  /**
   * Return a unit given its acronym
   * @param acronym
   * @return unit
   */
  @Override
  public synchronized IUnit getByAcronym(String acronym) {
    acronym = Objects.requireNonNull(acronym, "acronym may not be null").toUpperCase(Locale.US);
    if(acronym.isEmpty()) {
      throw new IllegalArgumentException("acronym may not be empty");
    }
    //nb : we no longer pull individual units from db, but rather just refer to the cache
    //initialising it if rqd (0.6.3)
    if(!unitCacheInitialised()) {
      initUnitCache();
    }
    return _unitsByAcronym.get(acronym);
  }

  /**
   * Return a unit given its name.
   * As of 0.6.3 this will be case-insensitive
   * @param name
   *          may not be null or empty
   * @return unit
   */
  @Override
  public synchronized IUnit getByName(String name) {
    name = Objects.requireNonNull(name, "name may not be null").toUpperCase(Locale.US);
    name = name.trim();
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    //nb : we no longer pull individual units from db, but rather just refer to the cache
    //initialising it if rqd (0.6.3)
    if(!unitCacheInitialised()) {
      initUnitCache();
    }
    return _unitsByName.get(name);
//    try(Connection connection = _dataSource.getConnection()) {
//      String sql = "SELECT * FROM UNITS WHERE UCASE(name)=?;";
//      ResultSet rs = DbUtils.readQuery(sql, new Object[] { name.toUpperCase() }, connection);
//      IUnit unit = null;
//      if(rs.next()) {
//        unit = readUnit(rs);
//      }
//      rs.getStatement().close();
//      return unit;
//    } catch(Exception e) {
//      throw new RuntimeException("Error loading unit by name " + name, e);
//    }
  }

  /**
   * Returns an ordered list of scanner units
   * @return scanners
   */
  @Override
  public synchronized List<IUnit> getScanners() {
    List<IUnit> data = getUnits();
    List<IUnit> records = new LinkedList<IUnit>();
    for(IUnit unit : data) { //Iterate all the units and add to the list any that have a scanner range
      if(unit.getScanner() > 0) {
        records.add(unit);
      }
    }
    Collections.sort(records, new BeanComparator("scanner"));
    return records;
  }
  
  @Override
  public void dataChangedExternally() {
    flushAllCaches();
    notifyDataChanged();
  }
  
  /**
   * Read information for a unit from a ResultSet row
   * @param rs
   * @return unit
   */
  private IUnit readUnit(ResultSet rs) {
    try {
      String xf = rs.getString("xfactor");
      XFactorId xfId = (xf==null || xf.isEmpty()) ? null : new XFactorId(xf);
      UnitImpl unit = new UnitImpl(          
              new UnitId(rs.getString("id")), 
              rs.getString("name"), 
              rs.getInt("attacks"),
              rs.getInt("offense"),
              rs.getInt("defense"),
              rs.getInt("pd"), 
              rs.getInt("speed"), 
              rs.getInt("carry"),
              rs.getInt("cost"),
              rs.getInt("complexity"),
              rs.getInt("scanner"), 
              rs.getInt("license"),
              rs.getString("acronym"), 
              xfId);      
      return unit;
    } catch(Exception e) {
      throw new RuntimeException("Error reading unit row from result set", e);
    }
  }

  /**
   * Shared by both saveUnit and saveUnits this method does the actual work of
   * saving a single unit to the database either via an update or insert using
   * the provided connection. Management of connection and catching of errors is
   * the responsibility of the calling method.
   * @param connection
   * @param unit
   * @return id
   */
  private UnitId saveUnitInternal(Connection connection, IUnit unit) throws Exception {
    Objects.requireNonNull(unit, "unit may not be null");
    if(unit.getName().isEmpty()) {
      throw new IllegalArgumentException("unit.name may not be empty");
    }
    if(unit.getAcronym().isEmpty()) {
      throw new IllegalArgumentException("unit.acronym may not be empty");
    }
    UnitId id = null;
    boolean isNew = unit.getId() == null;
    if(isNew) {
      id = new UnitId(_keyGen.createIdKey());
      String sql = "INSERT INTO UNITS" + " (id,name,attacks,offense,defense,pd,speed,carry,"
          + "cost,complexity,scanner,license,acronym,xfactor)" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      Object[] parameters_insert = new Object[] { id, unit.getName(), unit.getAttacks(), unit.getOffense(),
          unit.getDefense(), unit.getPd(), unit.getSpeed(), unit.getCarry(), unit.getCost(), unit.getComplexity(),
          unit.getScanner(), unit.getLicense(), unit.getAcronym(), unit.getXFactor(), };
      DbUtils.writeQuery(sql, parameters_insert, connection);
    } else {
      String sql = "UPDATE UNITS SET " + "name=?," + "attacks=?," + "offense=?," + "defense=?," + "pd=?," + "speed=?,"
          + "carry=?," + "cost=?," + "complexity=?," + "scanner=?," + "license=?," + "acronym=?," + "xfactor=? "
          + " WHERE id=?;";
      Object[] parameters_update = new Object[] { unit.getName(), unit.getAttacks(), unit.getOffense(),
          unit.getDefense(), unit.getPd(), unit.getSpeed(), unit.getCarry(), unit.getCost(), unit.getComplexity(),
          unit.getScanner(), unit.getLicense(), unit.getAcronym(), unit.getXFactor(), unit.getId() };
      DbUtils.writeQuery(sql, parameters_update, connection);
      id = unit.getId();
    }
    return id;
  }
  
  
  /**
   * Returns true if cached unit data is loaded, but does not check the cached
   * ranges calculations 
   * @return
   */
  private boolean unitCacheInitialised() {
    return _unitsCache != null;
  }
  
  private boolean rangeCacheInitialised() {
    return _ranges != null;
  }
  
  private boolean speedCacheInitialised() {
    return _speeds != null;
  }
  
  /**
   * Clears all the cached data
   */
  private void flushAllCaches() {
    _unitsCache = null;
    _unitsById = null;
    _unitsByName = null;
    _unitsByAcronym = null;
    _speeds = null;    
    _ranges = null;
  }
  
  /**
   * Read all the units from the database and initialise the unit cache and its lookup map
   * (This also flushes the other derived caches- ie speeds and ranges)
   */
  private void initUnitCache() {
    flushAllCaches();
    List<IUnit> units = new ArrayList<>();
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "SELECT * FROM UNITS  ORDER BY name ASC;";
      ResultSet rs = DbUtils.readQuery(sql, null, connection);
      while(rs.next()) {
        units.add(readUnit(rs));
      }
      rs.getStatement().close();
      units = Collections.unmodifiableList(units);
    } catch(Exception e) {
      throw new RuntimeException("Error loading all units ", e);
    }
    _unitsCache = units;
    _unitsById = new HashMap<UnitId, IUnit>();
    _unitsByName = new HashMap<String, IUnit>();
    _unitsByAcronym = new HashMap<String, IUnit>();
    for(IUnit unit : units) {
      _unitsById.put(unit.getId(), unit);
      String name = unit.getName();
      if(name!=null && !name.isEmpty()) {
        _unitsByName.put(name.toUpperCase(Locale.US), unit);
      }
      String acronym = unit.getAcronym();
      if(acronym!=null && !acronym.isEmpty()) {
        _unitsByAcronym.put(acronym.toUpperCase(Locale.US), unit);
      }
    }
  }

  /**
   * Initialise the cached array of units speed.
   * The array will always contain the 0 speed, even if there are no units having it.
   * (Since 0.6.3 we do this from the cached unit data rather than hitting the db)
   */
  private void initSpeedCache() {
    Set<Integer> speeds = new TreeSet<>();
    for(IUnit unit : getUnits() ) {
      speeds.add( unit.getSpeed() );
    }
    if(!speeds.contains(0)) {
      speeds.add(0);
    }
    _speeds = Convert.toPrimitiveIntArray(speeds);
  }
  
  /**
   * Check the name and acronym against the cache for to ensure it is not duplicating that
   * of same other unit
   * @param unit
   */
  private void checkDuplicate(IUnit unit) {
    Objects.requireNonNull(unit, "unit may not be null");
    Objects.requireNonNull(unit.getName(), "unit.name may not be null");
    Objects.requireNonNull(unit.getAcronym(), "unit.acronym may not be null");
    {
      IUnit other = getByName( unit.getName() );
      if(other != null && !other.getId().equals(unit.getId())) {
        throw new DuplicateRecordException("Unit name may not be the same as an existing unit: " + unit.getName());
      }
    }
    {
      IUnit other = getByAcronym( unit.getAcronym() );
      if(other != null && !other.getId().equals(unit.getId())) {
        throw new DuplicateRecordException("Unit acronym may not be the same as an existing unit: " + unit.getAcronym());
      } 
    }
  }
}
