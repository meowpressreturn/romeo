package romeo.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maintains a set of keyed values that may be incremented or added to
 */
public class Accumulator {
  //nb: May as well refactor this to use normal Integers. I dont think there is an appreciable
  //    difference using them compared to MutableInteger anymore(?)
  protected Map<Object, MutableInteger> _map = new HashMap<Object, MutableInteger>();

  /**
   * No-args constructor
   */
  public Accumulator() {
    ;
  }

  /**
   * Add qty to the value under specified key. If there is no such key it will
   * be created automatically.
   * @param key
   * @param qty
   */
  public void addToValue(Object key, int qty) {
    Objects.requireNonNull(key,"key may not be null");
    MutableInteger value = (MutableInteger) _map.get(key);
    if(value == null) {
      value = new MutableInteger(qty);
      _map.put(key, value);
    } else {
      value.setValue(value.intValue() + qty);
    }
  }

  /**
   * Return the value under the specified key. Will return 0 if there is no such
   * key.
   * @param key may not be null
   * @return value
   */
  public int getValue(Object key) {
    Objects.requireNonNull(key,"key may not be null");
    MutableInteger value = (MutableInteger) _map.get(key);
    return value == null ? 0 : value.intValue();
  }

  /**
   * Set the value under the specified key
   * @param key may not be null
   * @param qty
   *          new amount to set value to
   */
  public void setValue(Object key, int qty) {
    Objects.requireNonNull(key,"key may not be null");
    MutableInteger value = (MutableInteger) _map.get(key);
    if(value == null) {
      value = new MutableInteger(qty);
      _map.put(key, value);
    } else {
      value.setValue(qty);
    }
  }

  /**
   * Removes all the values and their keys
   */
  public void clear() {
    _map = new HashMap<Object, MutableInteger>();
  }

  /**
   * Returns all the keys for which values are stored (this includes explicitly added keys
   * whose value is currently zero)
   * @return keys
   */
  public Object[] getKeys() {
    Object[] keys = new Object[_map.size()];
    int index = 0;
    for(Object key : _map.keySet()) {
      keys[index++] = key;
    }
    return keys;
  }
}
