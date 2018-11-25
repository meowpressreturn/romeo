package romeo.xfactors.expressions;

import java.util.Objects;
import java.util.Random;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

/**
 * Implements the RND expression that generates random numbers. See the xfactor
 * reference help in the resources folder for details.
 * Immutable.
 */
public class Rnd implements IExpression {
  
  private final Random _rnd = new Random();
  private final int _min;
  private final int _range;

  /**
   * Constructor
   * @param min
   *          min value
   * @param max
   *          max value
   */
  public Rnd(final int min, final int max) {
    final int _max;
    if(max < min) { //terbalik
      _min = max;
      _max = min;
    } else {
      _min = min;
      _max = max;
    }
    _range = _max + 1 - _min;    
    if(_range<=1) {
      throw new IllegalArgumentException("max and min may not be equal");
    }
  }

  /**
   * Returns this expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "RND(" + _min + "," + (_range + _min) + ")";
  }

  /**
   * Returns a random value between min and max inclusive
   * @param context (unused for now but still required)
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    Objects.requireNonNull(context, "context may not be null");
    try {
      return new Integer(_rnd.nextInt(_range) + _min);
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public int getMin() {
    return _min;
  }
  
  public int getMax() {
    return _min+_range-1;
  }
  
  public int getRange() {
    return _range;
  }
}
