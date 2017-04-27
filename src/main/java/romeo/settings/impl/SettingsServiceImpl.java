package romeo.settings.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.sql.DataSource;

import romeo.persistence.AbstractPersistenceService;
import romeo.settings.api.ISettingsService;
import romeo.settings.api.SettingNotFoundException;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;

public class SettingsServiceImpl extends AbstractPersistenceService implements ISettingsService {
  
  private static final String TABLE_LONG = "SETTINGS_LONG";
  private static final String TABLE_STRING = "SETTINGS_STRING";
  private static final String TABLE_DOUBLE = "SETTINGS_DOUBLE";
  private static final String TABLE_FLAG = "SETTINGS_FLAG";

  /**
   * Shared with the {@link SettingsServiceInitialiser}, this returns the db table name for the
   * specified setting type
   * @param type
   * @return table
   */
  protected static String tableName(SettingType type) {
    switch (type){
      case LONG:
        return TABLE_LONG;
      case STRING:
        return TABLE_STRING;
      case DOUBLE:
        return TABLE_DOUBLE;
      case FLAG:
        return TABLE_FLAG;
      default:
        throw new UnsupportedOperationException("Unsupported type " + type);
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private Map<String, Long> _longCache;
  private Map<String, String> _stringCache;
  private Map<String, Double> _doubleCache;
  private Map<String, Boolean> _flagCache;

  public SettingsServiceImpl(DataSource dotaSource, IKeyGen keyGen) {
    super(dotaSource, keyGen);
    flushCache();
  }

  @Override
  public synchronized void setLong(String name, long value) {
    setSetting(name, SettingType.LONG, value);
  }

  @Override
  public synchronized long getLong(String name) {
    Long l = (Long) getSetting(name, SettingType.LONG);
    if(l == null) {
      throw new SettingNotFoundException(name, SettingType.LONG);
    }
    return l.longValue();
  }

  @Override
  public synchronized void setString(String name, String value) {
    setSetting(name, SettingType.STRING, value);
  }

  @Override
  public synchronized String getString(String name) {
    String s = (String) getSetting(name, SettingType.STRING);
    if(s == null) {
      throw new SettingNotFoundException(name, SettingType.STRING);
    }
    return s;
  }

  @Override
  public synchronized void setDouble(String name, double value) {
    setSetting(name, SettingType.DOUBLE, value);
  }

  @Override
  public synchronized double getDouble(String name) {
    Double d = (Double) getSetting(name, SettingType.DOUBLE);
    if(d == null) {
      throw new SettingNotFoundException(name, SettingType.DOUBLE);
    }
    return d.doubleValue();
  }

  @Override
  public synchronized void setFlag(String name, boolean value) {
    setSetting(name, SettingType.FLAG, value);
  }

  @Override
  public synchronized boolean isFlagSet(String name) {
    Boolean b = (Boolean) getSetting(name, SettingType.FLAG);
    if(b == null) {
      throw new SettingNotFoundException(name, SettingType.FLAG);
    }
    return b.booleanValue();
  }

  @Override
  public synchronized void setSetting(String name, SettingType type, Object value) {
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(value, "value must not be null");
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "MERGE INTO " + tableName(type) + " S" + " USING (VALUES ?,?) V (name, value)"
          + " ON (S.name = V.name)" + " WHEN MATCHED THEN UPDATE SET S.name = V.name, S.value=V.value"
          + " WHEN NOT MATCHED THEN INSERT (name,value) VALUES (V.name, V.value);";
      Object[] parameters = new Object[] { name, value };
      DbUtils.writeQuery(sql, parameters, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error writing to " + type + " setting \"" + name + "\"", e);
    }
    //Update value in the cache too
    putCache(name, type, value);
    SettingChangedEvent event = new SettingChangedEvent(this, name, type, value);
    notifyDataChanged(event);
  }

  @Override
  public synchronized Object getSetting(String name, SettingType type) {
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(type, "type must not be null");
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    Object value = getCache(name, type);
    if(value != null) {
      return value;
    }
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "SELECT value FROM " + tableName(type) + " WHERE name=?;";
      ResultSet rs = DbUtils.readQuery(sql, new Object[] { name }, connection);
      if(rs.next()) {
        value = rs.getObject(1);
        putCache(name, type, value);
        return value;
      } else {
        return null;
      }
    } catch(Exception e) {
      throw new RuntimeException("Error reading " + type + " setting \"" + name + "\"", e);
    }
  }

  @Override
  public synchronized boolean settingDefined(String name, SettingType type) {
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(type, "type must not be null");
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    //If theres a value in the cache then the setting is defined, no need to hit the
    //database. Return true immediately.
    Object value = getCache(name, type);
    if(value != null) {
      return true;
    }
    //Otherwise we must check the database, as there may be such a setting, but not yet cached
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "SELECT COUNT(*) FROM " + tableName(type) + " WHERE name=?;";
      ResultSet rs = DbUtils.readQuery(sql, null, connection);
      rs.next();
      int count = rs.getInt(1);
      return count > 0;
    } catch(Exception e) {
      throw new RuntimeException("Error searching for " + type + " setting \"" + name + "\"", e);
    }
  }
  
  private void putCache(String name, SettingType type, Object value) {
    switch (type){
      case LONG:
        _longCache.put(name, (Long) value);
        break;
      case STRING:
        _stringCache.put(name, (String) value);
        break;
      case DOUBLE:
        _doubleCache.put(name, (Double) value);
        break;
      case FLAG:
        _flagCache.put(name, (Boolean) value);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported type " + type);
    }
  }

  private Object getCache(String name, SettingType type) {
    Object value = null;
    switch (type){
      case LONG:
        value = _longCache.get(name);
        break;
      case STRING:
        value = _stringCache.get(name);
        break;
      case DOUBLE:
        value = _doubleCache.get(name);
        break;
      case FLAG:
        value = _flagCache.get(name);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported type " + type);
    }
    return value;
  }
  
  private void flushCache() {
    _longCache = new TreeMap<String, Long>();
    _stringCache = new TreeMap<String, String>();
    _doubleCache = new TreeMap<String, Double>();
    _flagCache = new TreeMap<String, Boolean>();
  }

}
