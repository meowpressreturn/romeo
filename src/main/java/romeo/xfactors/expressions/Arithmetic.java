/*
 * Arithmetic.java
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
 * Implements the ARITHMETIC expression. This is used to perform various simple
 * maths operations. See the xfactor reference help in the resources folder for
 * details.
 */
public class Arithmetic implements IExpression {
  /**
   * Addition operand
   */
  public static final int ADD = 0;

  /**
   * Subtraction operand
   */
  public static final int SUBTRACT = 1;

  /**
   * Multiplication operand
   */
  public static final int MULTIPLY = 2;

  /**
   * Division operand
   */
  public static final int DIVIDE = 3;

  /**
   * Minimum (of the 2 expression results) operand
   */
  public static final int MIN = 4;

  /**
   * Maximum (of the 2 expressions) operand
   */
  public static final int MAX = 5;

  /**
   * Square root operand
   */
  public static final int ROOT = 6;

  /**
   * Exponential power operand
   */
  public static final int POWER = 7;

  /**
   * Array mapping operands textual representation to the appropriate constant
   * (as the index)
   */
  public static final String[] OPERAND_TEXT = new String[8];
  static {
    OPERAND_TEXT[ADD] = "ADD";
    OPERAND_TEXT[SUBTRACT] = "SUBTRACT";
    OPERAND_TEXT[MULTIPLY] = "MULTIPLY";
    OPERAND_TEXT[DIVIDE] = "DIVIDE";
    OPERAND_TEXT[MIN] = "MIN";
    OPERAND_TEXT[MAX] = "MAX";
    OPERAND_TEXT[ROOT] = "ROOT";
    OPERAND_TEXT[POWER] = "POWER";
  }

  protected int _operand;
  protected IExpression _left;
  protected IExpression _right;

  /**
   * Takes the expression parameter string and parses it using the supplied
   * parser to initialise this expression object.
   * @param params
   * @param parser
   */
  public Arithmetic(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    try {
      String[] tokens = parser.tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      _left = parser.getExpression(tokens[0]);
      String operandToken = tokens[1].toUpperCase(Locale.US);
      _operand = Convert.toIndex(operandToken, OPERAND_TEXT);
      _right = parser.getExpression(tokens[2]);
      validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise ARITHMETIC with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param left
   *          the left expression
   * @param operand
   * @param right
   *          the right expression
   */
  public Arithmetic(IExpression left, int operand, IExpression right) {
    _left = Objects.requireNonNull(left, "left may not be null");
    _right = Objects.requireNonNull(right, "right may not be null");
    _operand = operand;
    validate();
  }

  /**
   * 
   * Validates that the operand is one of the valid constants
   * @throws IllegalStateException
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalArgumentException("Bad operand:" + _operand);
    }
  }

  /**
   * Returns this expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "ARITHMETIC(" + _left + "," + OPERAND_TEXT[_operand] + "," + _right + ")";
  }

  /**
   * Evaulates this expression and returns the result
   * @param context
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      double left = evalDouble(_left, context);
      double right = evalDouble(_right, context);
      switch (_operand){
        case ADD:
          return new Double(left + right);
        case SUBTRACT:
          return new Double(left - right);
        case MULTIPLY:
          return new Double(left * right);
        case DIVIDE:
          return new Double(left / right);
        case MIN:
          return left < right ? new Double(left) : new Double(right);
        case MAX:
          return left > right ? new Double(left) : new Double(right);
        case ROOT:
          if(right != 2d) {
            throw new UnsupportedOperationException("Only square root supported currently");
          }
          return new Double(Math.sqrt(left));
        case POWER:
          return new Double(Math.pow(left, right));
        default:
          throw new IllegalStateException("Illegal operand:" + _operand);
      }
    } catch(UnsupportedOperationException unsup) {
      throw unsup;
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }

  /**
   * Converts the result of the expression to a primitive double
   * @param expression
   * @param context
   * @return result
   */
  public static double evalDouble(IExpression expression, RoundContext context) {
    Objects.requireNonNull(expression, "expression may not be null");
    Objects.requireNonNull(context, "context may not be null");
    Object value = expression.evaluate(context);
    return Convert.toDouble(value);
  }
  
  public int getOperand() {
    return _operand;
  }
  
  public IExpression getLeft() {
    return _left;
  }
  
  public IExpression getRight() {
    return _right;
  }
}
