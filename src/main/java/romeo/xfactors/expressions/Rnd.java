package romeo.xfactors.expressions;

import java.util.Objects;
import java.util.Random;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;

/**
 * Implements the RND expression that generates random numbers. See the xfactor
 * reference help in the resources folder for details.
 */
public class Rnd implements IExpression {
  
  protected Random _rnd = new Random();
  private int _min;
  private int _range;

  /**
   * Constructor that parses the params string
   * @param params
   * @param parser
   */
  public Rnd(String params, IExpressionParser parser, IExpressionTokeniser tokeniser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    Objects.requireNonNull(tokeniser, "tokeniser may not be null");
    try {
      String[] tokens = tokeniser.tokenise(params);
      if(tokens.length != 2) {
        throw new IllegalArgumentException("Expecting 2 parameters but found " + tokens.length);
      }
      _min = Integer.parseInt(tokens[0]);
      int max = Integer.parseInt(tokens[1]);
      if(max < _min) { //If round the wrong way swap them
        int min = _min;
        _min = max;
        max = min;
      }
      _range = max+1 - _min;
      if(_range<=1) {
        throw new IllegalArgumentException("max and min may not be equal");
      }
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise RND with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param min
   *          min value
   * @param max
   *          max value
   */
  public Rnd(int min, int max) {
    _min = min;
    if(max < min) { //If round the wrong way swap them
      _min = max;
      max = min;
    }
    _range = max+1 - _min;    
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
}
