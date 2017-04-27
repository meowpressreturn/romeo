package romeo.settings.api;

import romeo.settings.api.ISettingsService.SettingType;

/**
 * Exception raised by the settings service if an attempt is made to read a
 * setting that does not exist (for that type)
 */
public class SettingNotFoundException extends RuntimeException {
  protected String _name;
  protected SettingType _type;

  public SettingNotFoundException(String name, SettingType type) {
    super("A value for " + type + " setting \"" + name + "\" was not found");
  }

  public String getName() {
    return _name;
  }

  public SettingType getType() {
    return _type;
  }

}
