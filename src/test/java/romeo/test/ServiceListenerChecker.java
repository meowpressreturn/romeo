package romeo.test;

import java.util.EventObject;

import romeo.model.api.IServiceListener;

/**
 * Utility class for tests to check if service listeners have been callled.
 */
public class ServiceListenerChecker implements IServiceListener {
  
  private EventObject _lastEvent;
  private int _dataChangedCount;
  
  public void reset() {
    _lastEvent = null;
    _dataChangedCount = 0;
  }

  @Override
  public void dataChanged(EventObject event) {
    _lastEvent = event;
    _dataChangedCount++;
  }
  
  public boolean wasCalled() {
    return _dataChangedCount > 0;
  }
  
  public int getDataChangedCount() {
    return _dataChangedCount;
  }
  
  public EventObject getLastEvent() {
    return _lastEvent;
  }

}



















