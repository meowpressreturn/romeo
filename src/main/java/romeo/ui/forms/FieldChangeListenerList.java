package romeo.ui.forms;

import java.util.Vector;

public class FieldChangeListenerList {
  protected Vector<IFieldChangeListener> _list = new Vector<IFieldChangeListener>(1);

  public FieldChangeListenerList() {
    ;
  }

  public synchronized void addFieldChangeListener(IFieldChangeListener listener) {
    if(!_list.contains(listener)) {
      _list.add(listener);
    }
  }

  public synchronized void removeFieldChangeListener(IFieldChangeListener listener) {
    _list.remove(listener);
  }

  public synchronized void notifyFieldChangeListeners(Object field) {
    //or should I go back to cloning the list first?
    for(int i = 0; i < _list.size(); i++) {
      ((IFieldChangeListener) _list.get(i)).valueChanged(field);
    }
  }

}
