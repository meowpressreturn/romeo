package romeo.settings.impl;

import java.util.EventObject;

import romeo.model.impl.AbstractService;
import romeo.settings.api.ISettingsService;

public class MockSettingsService extends AbstractService implements ISettingsService {

  /**
   * Made public for testing purposes
   */
  @Override
  public void notifyDataChanged(EventObject event) {
    super.notifyDataChanged(event);
  }
  
  @Override
  public void setSetting(String name, SettingType type, Object value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Object getSetting(String name, SettingType type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean settingDefined(String name, SettingType type) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setLong(String name, long value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public long getLong(String name) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setString(String name, String value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getString(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setDouble(String name, double value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public double getDouble(String name) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isFlagSet(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setFlag(String name, boolean value) {
    // TODO Auto-generated method stub
    
  }

}



















