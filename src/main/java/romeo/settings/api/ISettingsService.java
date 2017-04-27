package romeo.settings.api;

import romeo.model.api.IService;
import romeo.settings.impl.SettingChangedEvent;

/**
 * The settings service allows for simple persistence of single values that can
 * be used to maintain state between invocations of the application or to store
 * preference or other such like information. The service supports settings of
 * type long, double, and String. Null values are not permitted to be set.
 * Attempting to read a non existent setting will result in a SettingNotFound
 * runtime exception being raised. Values for each of the types exist in their
 * own namespace. So get/setLong for setting "foo" will read and write a value
 * independently of get/setString for "foo". Changes to values of a setting will
 * result in a {@link SettingChangedEvent} being fired and these will include
 * the new value of the setting. The implementation is expected to perform some
 * caching of setting values to allow for more efficiently reading settings
 * multiple times.
 */
public interface ISettingsService extends IService {
  public enum SettingType {
    LONG, STRING, DOUBLE, FLAG;
  };

  /**
   * Create or update the specified setting. Null values are not permitted.
   * @param name
   * @param type
   * @param value
   */
  public void setSetting(String name, SettingType type, Object value);

  /**
   * Returns the value for the specified setting and type if it exists. If it
   * doesn't exist then null is returned (doesn't throw exception).
   * @param name
   * @param type
   * @return
   */
  public Object getSetting(String name, SettingType type);

  /**
   * Returns true if the named setting exists for the specified type.
   * @param name
   * @param type
   * @return
   */
  public boolean settingDefined(String name, SettingType type);

  /**
   * Create or update a long integer setting value.
   * @param name
   * @param value
   */
  public void setLong(String name, long value);

  /**
   * Read a value for a long integer setting. If no long integr setting for the
   * specified name is present then a {@link SettingNotFoundException} is
   * raised.
   * @param name
   * @return
   */
  public long getLong(String name);

  /**
   * Create or update a text setting value.
   * @param name
   * @param value
   */
  public void setString(String name, String value);

  /**
   * Get a text setting value. If there is no text setting value for the
   * specified name then a {@link SettingNotFoundException} runtime exception is
   * raised.
   * @param name
   * @return
   */
  public String getString(String name);

  /**
   * Create or update a double precision floating point value setting.
   * @param name
   * @param value
   */
  public void setDouble(String name, double value);

  /**
   * Read a double precision floating point value setting. If a double precision
   * floating point setting value for the sepcified name is not found then a
   * runrtime exception of type {@link SettingNotFoundException} will be thrown.
   * @param name
   * @return
   */
  public double getDouble(String name);

  /**
   * Returns the value of a boolean setting
   * @param name
   * @return
   */
  public boolean isFlagSet(String name);

  /**
   * Set a boolean setting
   * @param name
   * @param value
   */
  public void setFlag(String name, boolean value);
}
