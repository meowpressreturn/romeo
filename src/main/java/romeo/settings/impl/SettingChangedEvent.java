package romeo.settings.impl;

import java.util.EventObject;

import romeo.settings.api.ISettingsService;
import romeo.settings.api.ISettingsService.SettingType;

/**
 * Event that indicates a setting changed and its new value
 */
public class SettingChangedEvent extends EventObject {
  protected String _name;
  protected SettingType _type;
  protected Object _value;

  /**
   * Returns the name of the setting that changed
   * @return setting name
   */
  public String getName() {
    return _name;
  }

  public SettingType getType() {
    return _type;
  }

  /**
   * Returns the new value of the setting
   * @return value
   */
  public Object getValue() {
    return _value;
  }

  /**
   * Returns the value of a String setting in this event. If the setting is not
   * a String setting then a ClassCastException is thrown.
   * @return value
   */
  public Object getStringValue() {
    if(_type == SettingType.STRING) {
      return (String) _value;
    } else {
      throw new ClassCastException("setting " + _name + " is of type " + _type + " and not STRING");
    }
  }

  public long getLongValue() {
    if(_type == SettingType.LONG) {
      return (Long) _value;
    } else {
      throw new ClassCastException("setting " + _name + " is of type " + _type + " and not LONG");
    }
  }

  public double getDoubleValue() {
    if(_type == SettingType.DOUBLE) {
      return (Double) _value;
    } else {
      throw new ClassCastException("setting " + _name + " is of type " + _type + " and not DOUBLE");
    }
  }

  public SettingChangedEvent(ISettingsService source, String name, SettingType type, Object value) {
    super(source);
    _name = name;
    _type = type;
    _value = value;
  }

}
