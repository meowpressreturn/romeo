/*
 * NumericFieldConstraint.java
 * Created on Feb 6, 2006
 */
package romeo.ui.forms;

public class NumericFieldConstraint {
  private boolean _negativeAllowed = true;
  private long _minValue = Long.MIN_VALUE;
  private long _maxValue = Long.MAX_VALUE;
  private boolean _allowDecimal = false;

  public NumericFieldConstraint() {
    ;
  }

  public NumericFieldConstraint(long min, long max, boolean decimal) {
    setMinValue(min);
    setMaxValue(max);
    setAllowDecimal(decimal);
    if(min >= 0) {
      setAllowDecimal(false);
    }
  }

  public long getMaxValue() {
    return _maxValue;
  }

  public long getMinValue() {
    return _negativeAllowed ? _minValue : _minValue < 0 ? 0 : _minValue;
  }

  public boolean isNegativeAllowed() {
    return _negativeAllowed;
  }

  public void setMaxValue(long l) {
    _maxValue = l;
  }

  public void setMinValue(long l) {
    _minValue = l;
  }

  public void setNegativeAllowed(boolean b) {
    _negativeAllowed = b;
  }

  public boolean isAllowDecimal() {
    return _allowDecimal;
  }

  public void setAllowDecimal(boolean allowDecimal) {
    _allowDecimal = allowDecimal;
  }

}
