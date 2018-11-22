package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;

/**
 * Implements the VALUE expression. This expression is how actual values are
 * specified in XFEL. See the xfactor reference help in the resources folder for
 * details.
 */
public class Value implements IExpression {
  
  protected Object _value;

  /**
   * Constructor. This takes a value string. The strings null, true, and false
   * evaluate to null, boolean true or boolean false. Values that are quoted
   * with " are treated as strings, and other values are assumed to be numeric.
   * @param token
   *          the value to return
   * @param parser
   *          
   */
  public Value(String params, IExpressionParser parser, IExpressionTokeniser tokeniser) {
    
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may nto be null");
    String[] tokens = tokeniser.tokenise(params);
    if(tokens.length != 1) {
     throw new IllegalArgumentException("VALUE requires one and only one parameter"); 
    }
    String token = tokeniser.trimToken(tokens[0]);
    _value = Convert.toObject(token);
  }

  /**
   * Constructor
   * @param value
   *          value to be returned by evaluate()
   */
  public Value(Object value) {
    if(value == null || value instanceof Double || value instanceof Integer || value instanceof Long
        || value instanceof String || value instanceof Boolean) {
      _value = value;
    } else {
      throw new IllegalArgumentException("Invalid value type:" + value.getClass().getName());
    }
  }

  /**
   * Returns an XFEL string describing this expression
   * @return string
   */
  @Override
  public String toString() {
    return "VALUE(" + ((_value instanceof String) ? "\"" + _value + "\"" : _value) + ")";
  }

  /**
   * Boolean Constructor
   * @param value
   *          a boolean value
   */
  public Value(boolean value) {
    _value = value ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Double constructor
   * @param value
   *          a double value
   */
  public Value(double value) {
    _value = new Double(value);
  }

  
  public Value(long value) {
    _value = new Long(value);
  }
  
  /**
   * Int constructor
   * @param value
   *          an int value
   */
  public Value(int value) {
    _value = new Integer(value);
  }

  /**
   * Returns the prespecified value
   * @param context
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    return _value;
  }
  
  public Object getValue() {
    return _value;
  }
}
