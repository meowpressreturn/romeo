package romeo.utils;

import java.io.Serializable;

/**
 * Essentially just a holder class for a primitive boolean TODO impl Comparable
 */
public class MutableBoolean implements Serializable {
  private boolean _value;

  public MutableBoolean() {

  }

  public MutableBoolean(boolean value) {
    _value = value;
  }

  public void setValue(boolean value) {
    _value = value;
  }

  public boolean getValue() {
    return _value;
  }

  public boolean isTrue() {
    return _value;
  }

  public boolean isFalse() {
    return !_value;
  }
}
