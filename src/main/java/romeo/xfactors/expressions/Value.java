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
  
  /**
   * Utility method to convert a value token used in a VALUE operation. Applies the following heuristics:
   * if the text is "null" will return null,
   * if "true" or "false" (case insensitive) will return a Boolean,
   * if it starts with a '"' (double quote) will consider it a String and strip the leading and trailing quotes
   * (nb: currently unescaped internal quotes are not detected (they should cause a failure, but don't yet))
   * if it contains a '.' will try to return a Double (or fail if cannot parse as such),
   * otherwise will try to convert to a Long.
   * @param tokenText
   * @return
   */
  public static Object parseValue(String tokenText) {
    tokenText = Objects.requireNonNull(tokenText, "token may not be null").trim();
    final Object value;
    if("null".equalsIgnoreCase(tokenText)) {
      value = null;
    } else if("true".equalsIgnoreCase(tokenText)) {
      value = Boolean.TRUE;
    } else if("false".equalsIgnoreCase(tokenText)) {
      value = Boolean.FALSE;
    } else {
      if(tokenText.startsWith("\"")) { 
        value = Convert.toUnquotedString(tokenText);
      } else {
        //Assume its numeric
        if(tokenText.indexOf('.') != -1) {
          try {
            value = new Double(Double.parseDouble(tokenText));
          } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid numeric (double float) value:" + tokenText);
          }
        } else {
          try {
            value = new Long(Long.parseLong(tokenText));
          } catch(NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid numeric (long int) value:" + tokenText);
          }
        }
      }
    }
    return value;
  }
  
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
    _value = parseValue(token);
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
