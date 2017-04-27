package romeo.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wraps a List to manage data (which is often being accumulated for averaging
 * round data). The data is double values and these are maintained in instances
 * of a holder class {@link MutableDouble}.
 */
public class MutableDoubleListWrapper {
  protected List<MutableDouble> _data;

  /**
   * No-args constructor.
   */
  public MutableDoubleListWrapper() { //An arrayList is used to facilitate access by index
    _data = new ArrayList<MutableDouble>(32);
  }

  /**
   * Returns the data. The list returned should not be modified.
   * @return list
   */
  public List<MutableDouble> getData() {
    return Collections.unmodifiableList(_data);
  }

  /**
   * Returns the size of the wrapped list
   * @return size
   */
  public int getSize() {
    return _data.size();
  }

  /**
   * Expand or contract the list to the specified number of elements. Newly
   * created elements will hold the value 0. The old size is returned.
   * @param elements
   * @return oldSize
   */
  public int setSize(int elements) {
    int oldSize = _data.size();
    while(_data.size() > elements) {
      _data.remove(elements);
    }
    while(elements > _data.size()) {
      _data.add(new MutableDouble(0));
    }
    return oldSize;
  }

  /**
   * Returns an iterator over the list. The
   * iterator returned should not be used to modify the list.
   * @return iterator
   */
  public Iterator<MutableDouble> iterator() {
    return Collections.unmodifiableList(_data).iterator();
  }

  /**
   * Sets value into the list at the specified index, using a MutableDouble to
   * hold the value. The list is expanded to the necessary size for the
   * specified index to be valid by addind elements with value 0.
   * @param index
   * @param value
   */
  public void setValue(int index, double value) {
    if(index == _data.size()) { //If its the next one then just add it.
      _data.add(new MutableDouble(value));
    } else if(index > _data.size()) { //Add to the list until it has enough elements for index-1 to be a valid index
      for(int i = _data.size(); i < index; i++) {
        _data.add(new MutableDouble(0));
      }
      //And then add the new element at the end at what will be index
      _data.add(new MutableDouble(value));
    } else { //List is big enough. Replace an existing element. (It must have a holder there already)
      MutableDouble holder = (MutableDouble) _data.get(index);
      holder.setValue(value); //Modify existing value in holder to new value
    }
  }

  /**
   * Increment the existing value at the specified index by the delta. If
   * required the list will be expanded to fit index. The old value is returned.
   * @param index
   * @param delta
   * @return previousValue
   */
  public double addValue(int index, double delta) {
    if(index >= _data.size()) { //If the element doesnt exist then create and set it
      setValue(index, delta);
      return 0;
    } else { //Add to the existing value and return the old one
      MutableDouble holder = (MutableDouble) _data.get(index);
      double previousValue = holder.doubleValue();
      holder.addValue(delta);
      return previousValue;
    }
  }

  /**
   * Returns the value at the specified index. The index must be valid.
   * @param index
   * @return value
   */
  public double getValue(int index) {
    MutableDouble holder = (MutableDouble) _data.get(index);
    return holder.doubleValue();
  }

}
