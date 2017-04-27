package romeo.ui;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMapLogic implements GenericMap.IMapLogic {
  private List<GenericMap> _listeners = new ArrayList<GenericMap>(1);

  public void dispose() {
    _listeners.clear();
  }

  @Override
  public void addListener(GenericMap map) {
    if(!_listeners.contains(map)) {
      _listeners.add(map);
    }
  }

  public void removeListener(GenericMap map) {
    if(_listeners.contains(map)) {
      _listeners.remove(map);
    }
  }

  protected void notifyListeners() {
    List<GenericMap> list = new ArrayList<GenericMap>(_listeners); //Make safe copy
    for(GenericMap listener : list) {
      listener.refresh();
    }
  }

  @Override
  public boolean isSameObject(Object object1, Object object2) {
    if(object1 == null) {
      return object2 == null;
    }
    return object1.equals(object2);
  }
}
