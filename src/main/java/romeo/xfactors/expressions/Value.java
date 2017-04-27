/*
 * Value.java
 * Created on Mar 12, 2006
 */
package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;

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
  public Value(String params, IExpressionParser parser) {
    
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may nto be null");
    String[] tokens = parser.tokenise(params);
    if(tokens.length != 1) {
     throw new IllegalArgumentException("VALUE requires one and only one parameter"); 
    }
    String token = parser.trimToken(tokens[0]);
    
    if("null".equalsIgnoreCase(token)) {
      _value = null;
    } else if("true".equalsIgnoreCase(token)) {
      _value = Boolean.TRUE;
    } else if("false".equalsIgnoreCase(token)) {
      _value = Boolean.FALSE;
    } else {
      if(token.startsWith("\"")) { 
        _value = Convert.toUnquotedString(token);
      } else {
        //Assume its numeric
        if(token.indexOf('.') != -1) {
          try {
            _value = new Double(Double.parseDouble(token));
          } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid numeric (double float) value:" + token);
          }
        } else {
          try {
            _value = new Long(Long.parseLong(token));
          } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid numeric (long int) value:" + token);
          }
        }
      }
    }
  }

  /**
   * Constructor
   * @param value
   *          value to be returned by evaluate()
   */
  public Value(Object value) {
    if(value == null || value instanceof Double || value instanceof Integer || value instanceof Long
        || value instanceof String) {
      _value = value;
    } else {
      throw new IllegalArgumentException("Invalid value type");
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
