/*
 * Adjust.java
 * Created on Mar 13, 2006
 */
package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;

/**
 * Implements the ADJUST expression. This is used to round values. See the
 * xfactor reference help in the resources folder for details.
 */
public class Adjust implements IExpression {
  /**
   * Operand specifying to round the value
   */
  public static final int ROUND = 0;

  /**
   * Operand specifying to round down to nearest integer
   */
  public static final int FLOOR = 1;

  /**
   * Operand specifying to round up to next integer
   */
  public static final int CEILING = 2;

  /**
   * Array of text strings that map to operand constants
   * (This was done before we had enums in Java!)
   */
  public static final String[] OPERAND_TEXT = new String[3];
  static {
    OPERAND_TEXT[ROUND] = "ROUND";
    OPERAND_TEXT[FLOOR] = "FLOOR";
    OPERAND_TEXT[CEILING] = "CEILING";
  }
  
  public static int asOperand(String text) {
    String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
    return Convert.toIndex(operandToken, OPERAND_TEXT);
  }

  protected IExpression _value;
  protected int _operand;

  /**
   * Constructor that parses the string parameters to initialise this expression
   * object
   * @param params
   *          the parameters to the ADJUST expression
   * @param parser
   *          the XFEL parser
   */
  public Adjust(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    try {
      String[] tokens = parser.tokenise(params);
      if(tokens.length != 2) {
        throw new IllegalArgumentException("Expecting 2 parameters but found " + tokens.length);
      }
      _value = parser.getExpression(tokens[0]);
      _operand = asOperand(tokens[1]);
      validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise ADJUST with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param valueExpression
   *          Expression that returns the value to be adjusted
   * @param operand
   *          ajustment operation to be performed on the value to give return
   *          value
   */
  public Adjust(IExpression valueExpression, int operand) {
    _value = Objects.requireNonNull(valueExpression, "valueExpression may not be null");
    _operand = operand;
    validate();
  }

  /**
   * Throws an exception of the operarand is not one of the valid constants
   * @throws IllegalStateException
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalArgumentException("Bad operand for Adjust:" + _operand);
    }
  }

  /**
   * Returns the expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "ADJUST(" + _value + "," + OPERAND_TEXT[_operand] + ")";
  }

  /**
   * Evaluate this expresssion and return its result
   * @param context
   * @return result
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      double value = Arithmetic.evalDouble(_value, context);
      switch (_operand){
        case ROUND:
          return new Double(Math.round(value));
        case FLOOR:
          return new Double(Math.floor(value));
        case CEILING:
          return new Double(Math.ceil(value));
        default:
          throw new IllegalArgumentException("Illegal operand:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public int getOperand() {
    return _operand;
  }
  
  public IExpression getValue() {
    return _value;
  }

}
