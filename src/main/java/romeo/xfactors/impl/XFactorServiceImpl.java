package romeo.xfactors.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;

import romeo.persistence.AbstractPersistenceService;
import romeo.persistence.DuplicateRecordException;
import romeo.units.api.IUnitService;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.api.XFactorId;

/**
 * Provides the underlying logic for persisting xfactor definitions. 
 */
public class XFactorServiceImpl extends AbstractPersistenceService implements IXFactorService {
  
  //nb: since the service might be accessed rfrom multiple threads, methods that access these need to be
  //synchronized
  private List<IXFactor> _xFactorsCache;
  private Map<XFactorId, IXFactor> _xFactorLookupCache;
  private Map<String, IXFactor> _xFactorsByName;
  
  private IUnitService _unitService;

  public XFactorServiceImpl(Logger log, DataSource dataSource, IKeyGen keyGen, IUnitService unitService) {    
    super(log, dataSource, keyGen);
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
  }

  /**
   * Delete an xfactor from the database and notify listeners
   * @param id
   */
  @Override
  public synchronized void deleteXFactor(XFactorId id) {
    Objects.requireNonNull(id, "id may not be null");
    _unitService.unlinkUnitsWithXFactor(id);
    try(Connection connection = _dataSource.getConnection()) {
      final String sql = "DELETE FROM XFACTORS WHERE id=?;";
      DbUtils.writeQuery(sql, new Object[] { id }, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error deleting xFactor " + id, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  /**
   * Load an xfactor given its name
   * @param name
   * @return xFactor
   */
  @Override
  public synchronized IXFactor getXFactorByName(String name) {
    name = Objects.requireNonNull(name, "name may not be null").toUpperCase(Locale.US);
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    name=name.trim().toUpperCase(Locale.US);
    if(!cacheInitialised()) {
      initCache();
    }
    return _xFactorsByName.get(name);
  }
  
  /**
   * Load an XFactor definition given its id
   * @param id
   * @return xFactor
   */
  @Override
  public synchronized IXFactor getXFactor(XFactorId id) {
    Objects.requireNonNull(id, "id may not be null");
    if(!cacheInitialised()) {
      initCache();
    }
    return _xFactorLookupCache.get(id);
  }

  /**
   * Returns a list of all the XFactor definitions
   * @return xFactors
   */
  @Override
  public synchronized List<IXFactor> getXFactors() {
    if(!cacheInitialised()) {
      initCache();
    }
    return Collections.unmodifiableList(_xFactorsCache);
  }

  /**
   * Save or update an xfactor in the database and notify listeners
   * @param xFactor
   * @return id
   */
  @Override
  public synchronized XFactorId saveXFactor(IXFactor xFactor) {
    Objects.requireNonNull(xFactor, "xFactor may not be null");
    XFactorId id = xFactor.getId(); //would be null if it is new
    if(xFactor.getName().isEmpty()) {
      throw new IllegalArgumentException("xFactor.name may not be empty");
    }
    checkDuplicate(xFactor);
    try(Connection connection = _dataSource.getConnection()) { 
      if(xFactor.isNew()) {
        id = new XFactorId( _keyGen.createIdKey() );
        final String sql = "INSERT INTO XFACTORS"
            + " (id,name,description,xfTrigger,xfAttacks,xfOffense,xfDefense,xfPd,xfRemove)"
            + " VALUES (?,?,?,?,?,?,?,?,?);";
        Object[] parameters_insert = new Object[] { id, xFactor.getName(), xFactor.getDescription(),
            xFactor.getTrigger(), xFactor.getXfAttacks(), xFactor.getXfOffense(), xFactor.getXfDefense(),
            xFactor.getXfPd(), xFactor.getXfRemove(), };
        DbUtils.writeQuery(sql, parameters_insert, connection);
      } else {
        final String sql = "UPDATE XFACTORS SET " + "name=?," + "description=?," + "xfTrigger=?," + "xfAttacks=?,"
            + "xfOffense=?," + "xfDefense=?," + "xfPd=?," + "xfRemove=?" + " WHERE id=?;";
        Object[] parameters_update = new Object[] { xFactor.getName(), xFactor.getDescription(), xFactor.getTrigger(),
            xFactor.getXfAttacks(), xFactor.getXfOffense(), xFactor.getXfDefense(), xFactor.getXfPd(),
            xFactor.getXfRemove(), xFactor.getId() };
        DbUtils.writeQuery(sql, parameters_update, connection);
      }
      return id;
    } catch(Exception e) {
      throw new RuntimeException("Error saving XFactor " + xFactor, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }
  
  private boolean cacheInitialised() {
    return _xFactorsCache != null;
  }

  /**
   * Clear cached information
   */
  private synchronized void flushCache() {
    _xFactorsCache = null;
    _xFactorLookupCache = null;
    _xFactorsByName = null;
  }

  /**
   * Initialise the cache from a list of {@link XFactor}
   * @param xFactors
   */
  private synchronized void initCache() {
    List<IXFactor> results = new ArrayList<>();
    try(Connection connection = _dataSource.getConnection()) {
      final String sql = "SELECT * FROM XFACTORS  ORDER BY name ASC;";
      ResultSet rs = DbUtils.readQuery(sql, null, connection);
      while(rs.next()) {
        results.add(readXFactor(rs));
      }
      rs.getStatement().close();
      results = Collections.unmodifiableList(results);
    } catch(Exception e) {
      throw new RuntimeException("Error loading all xFactors ", e);
    }    
    _xFactorsCache = results;
    _xFactorLookupCache = new HashMap<XFactorId, IXFactor>();
    _xFactorsByName = new HashMap<String, IXFactor>();
    for(IXFactor xf : _xFactorsCache) {
      _xFactorLookupCache.put(xf.getId(), xf);
      String name = xf.getName();
      if(name != null && !name.isEmpty()) {
        _xFactorsByName.put(xf.getName().toUpperCase(Locale.US), xf);
      }
    }
  }  

  /**
   * Read an XFactor definition from a row of a ResultSet
   * @param rs
   * @return xFactor
   */
  private IXFactor readXFactor(ResultSet rs) {
    Objects.requireNonNull(rs, "rs may not be null");
    try {
      XFactorId id = new XFactorId( rs.getString("id") );
      String name = rs.getString("name");
      String description = rs.getString("description");
      String trigger = rs.getString("xfTrigger"); //nb: HSQLDB 2 doesnt allow TRIGGER as column name
      String attacks = rs.getString("xfAttacks");
      String offense = rs.getString("xfOffense");
      String defense = rs.getString("xfDefense");
      String pd = rs.getString("xfPd");
      String remove = rs.getString("xfRemove");
      IXFactor xf = new XFactorImpl(id, name, description, trigger, attacks, offense, defense, pd, remove);
      return xf;
    } catch(Exception e) {
      throw new RuntimeException("Error reading XFactor row from result set", e);
    }
  }
  
  private void checkDuplicate(IXFactor xfactor) {
    Objects.requireNonNull(xfactor, "unit may not be null");
    Objects.requireNonNull(xfactor.getName(), "xfactor.name may not be null");
    IXFactor other = getXFactorByName( xfactor.getName() );
    if(other != null && !other.getId().equals(xfactor.getId())) {
      throw new DuplicateRecordException("X-Factor name may not be the same as an existing x-factor: " + xfactor.getName());
    }
  }

}
