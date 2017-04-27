/*
 * MutableInteger.java
 * Created on Feb 26, 2006
 */
package romeo.utils;

/**
 * Wrapper class for a primitive int value than can be modified.
 */
public class MutableInteger extends Number {
  protected int _value;

  /**
   * No-args constructor. Sets the initial value to 0.
   */
  public MutableInteger() {
    _value = 0;
  }

  /**
   * Constructor
   * @param value
   *          initial value
   */
  public MutableInteger(int value) {
    _value = value;
  }

  /**
   * Constructor
   * @param value
   *          initial value
   */
  public MutableInteger(Number value) {
    _value = value.intValue();
  }

  /**
   * Constructor that parses the string into an integer. If the string is
   * invalid will throw a {@link NumberFormatException} exception.
   * @param value
   *          initial value as a string
   */
  public MutableInteger(String value) {
    try {
      _value = Integer.parseInt(value);
    } catch(Exception e) {
      throw new NumberFormatException("Cannot convert to int:" + value);
    }
  }

  /**
   * Returns the wrapped value as an Integer
   * @return value
   */
  public Integer getValueInteger() {
    return new Integer(_value);
  }

  /**
   * Returns the wrapped value.
   * @return int
   */
  public int getValue() {
    return _value;
  }

  /**
   * Set the wrapped value to the specified value
   * @param value
   */
  public void setValue(int value) {
    _value = value;
  }

  /**
   * Increment the wrapped value by the specified amount
   * @param more
   */
  public void addValue(int more) {
    _value += more;
  }

  /**
   * Compares the value
   * @param obj
   * @return equals
   */
  @Override
  public boolean equals(Object obj) {
    int otherValue = 0;
    if(obj instanceof MutableInteger) {
      otherValue = ((MutableInteger) obj).getValue();
    } else if(obj instanceof Number) {
      otherValue = ((Number) obj).intValue();
    } else {
      return super.equals(obj);
    }
    return _value == otherValue;
  }

  /**
   * Returns the wrapped value directly as its hashcode. Since the value and
   * thus the hash are mutabvle you dont want to use these objects as keys in a
   * map.
   * @return hashCode
   */
  @Override
  public int hashCode() {
    return _value;
  }

  /**
   * Returns the value as a string
   * @return string
   */
  @Override
  public String toString() {
    return "" + _value;
  }

  /**
   * Returns the wrapped value as a double
   * @return value
   */
  @Override
  public double doubleValue() {
    return (double) _value;
  }

  /**
   * Returns the wrapped value as a float
   * @return value
   */
  @Override
  public float floatValue() {
    return (float) _value;
  }

  /**
   * Returns the wrapped value
   * @return value
   */
  @Override
  public int intValue() {
    return _value;
  }

  /**
   * Returns the wrapped value as a long
   * @return value
   */
  @Override
  public long longValue() {
    return (long) _value;
  }

}
