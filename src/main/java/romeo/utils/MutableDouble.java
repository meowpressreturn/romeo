/*
 * MutableDouble.java
 * Created on Mar 10, 2006
 */
package romeo.utils;

/**
 * Holder class for a primitive double that can be modified. With the autoboxing
 * support and various optimisations in modern JVMs it would be best if we
 * factored away this class.
 */
public class MutableDouble extends Number implements Comparable<Number> {
  protected double _value;

  /**
   * Constructor. Initial value is zero
   */
  public MutableDouble() {
    _value = 0;
  }

  @Override
  public int compareTo(Number o) {
    //    if(o instanceof Number)
    //    {  
    //      return Double.compare(_value, ((Number)o).doubleValue() );
    //    }
    //    else
    //    {
    //      throw new ClassCastException("not an instanceof Number");
    //    }
    return Double.compare(_value, ((Number) o).doubleValue());
  }

  /**
   * Constructor. Takes a primitive double.
   * @param value
   */
  public MutableDouble(double value) {
    _value = value;
  }

  /**
   * Constructor. Takes a number object.
   * @param value
   */
  public MutableDouble(Number value) {
    _value = value.doubleValue();
  }

  /**
   * Constructor that parses the specified string for the initial value.
   * @param value
   */
  public MutableDouble(String value) {
    try {
      _value = Double.parseDouble(value);
    } catch(Exception e) {
      throw new NumberFormatException("Cannot convert to double:" + value);
    }
  }

  /**
   * Gets the value as a Double object
   * @return value
   */
  public Double getValueDouble() {
    return new Double(_value);
  }

  /**
   * Gets the value as a primitive double
   * @return value
   */
  public double getValue() {
    return _value;
  }

  /**
   * Set the value to the specified value
   * @param value
   */
  public void setValue(double value) {
    _value = value;
  }

  /**
   * Increment the wrapped value by the specified amount
   * @param more
   */
  public void addValue(double more) {
    _value += more;
  }

  /**
   * Divide the wrapped value by the specified amount.
   * @param divideBy
   */
  public void divideValue(double divideBy) {
    _value /= divideBy;
  }

  /**
   * Returns true if this MutableDouble holds the same as another, or if it
   * holds the same as a Number that is passed.
   * @param object
   * @return equal
   */
  @Override
  public boolean equals(Object obj) {
    double otherValue = 0;
    if(obj instanceof MutableDouble) {
      otherValue = ((MutableDouble) obj).getValue();
    } else if(obj instanceof Number) {
      otherValue = ((Number) obj).doubleValue();
    } else {
      return super.equals(obj);
    }
    //Following logics of java.lang.Double
    return Double.doubleToLongBits(otherValue) == Double.doubleToLongBits(_value);
  }

  /**
   * Returns a hashcode based on the wrapped double. Since this object is
   * mutable and its hash can change too, using it as a map key is not a good
   * idea.
   * @return hashCode
   */
  @Override
  public int hashCode() { //Based on whats in Java.lang.Double
    long bits = Double.doubleToLongBits(_value);
    return (int) (bits ^ (bits >>> 32));
  }

  /**
   * Returns a strinbg representation of the wrapped value
   * @return string
   */
  @Override
  public String toString() {
    return "" + _value;
  }

  //----

  /**
   * Returns the wrapped value as a double
   * @return value
   */
  @Override
  public double doubleValue() {
    return _value;
  }

  /**
   * Returns the wrapped value as a float. Some loss of precision may occur.
   * @return value
   */
  @Override
  public float floatValue() {
    return (float) _value;
  }

  /**
   * Returns the wrapped value as an int. Loss of fractional information may
   * occur.
   * @return value
   */
  @Override
  public int intValue() {
    return (int) _value;
  }

  /**
   * Returns the wrapped value as a long. Loss of fractional information may
   * occur.
   * @return value
   */
  @Override
  public long longValue() {
    return (long) _value;
  }

}
