/*
 * Comparison.java
 * Created on Mar 12, 2006
 */
package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.BeanComparator;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;

/**
 * Implements the COMPARISON expression used to compare between values. See the
 * xfactor reference help in the resources folder for details.
 */
public class Comparison implements IExpression {
  /**
   * Not equals operand
   */
  public static final int NOT_EQUAL = 0;

  /**
   * Equals operand
   */
  public static final int EQUAL = 1;

  /**
   * Ggreater than operand
   */
  public static final int GREATER_THAN = 2;

  /**
   * Greater or equal operand
   */
  public static final int GREATER_OR_EQUAL = 3;

  /**
   * Less operand
   */
  public static final int LESS_THAN = 4;

  /**
   * Less or equal operand
   */
  public static final int LESS_OR_EQUAL = 5;

  /**
   * Array mapping operand text to constant (via its index)
   */
  public static final String[] OPERAND_TEXT = new String[6];
  static {
    OPERAND_TEXT[NOT_EQUAL] = "NOT_EQUAL";
    OPERAND_TEXT[EQUAL] = "EQUAL";
    OPERAND_TEXT[GREATER_THAN] = "GREATER_THAN";
    OPERAND_TEXT[GREATER_OR_EQUAL] = "GREATER_OR_EQUAL";
    OPERAND_TEXT[LESS_THAN] = "LESS_THAN";
    OPERAND_TEXT[LESS_OR_EQUAL] = "LESS_OR_EQUAL";
  }
  
  public static int asOperand(String text) {
    String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
    return Convert.toIndex(operandToken, OPERAND_TEXT);
  }

  protected IExpression _left;
  protected int _operand;
  protected IExpression _right;

  /**
   * Constructor that parses the params string
   * @param params
   * @param parser
   */
  public Comparison(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser mau not be null");
    try {
      String[] tokens = parser.tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      _left = parser.getExpression(tokens[0]);
      _operand = asOperand(tokens[1]);
      _right = parser.getExpression(tokens[2]);
      validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise COMPARISON with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param left
   *          expression that returns left value for comparison
   * @param operand
   * @param right
   *          expression returning right value for comparison
   */
  public Comparison(IExpression left, int operand, IExpression right) {
    _left = Objects.requireNonNull(left, "left may not be null");
    _operand = operand;
    _right = Objects.requireNonNull(right, "right may not be null");
    validate();
  }

  /**
   * Validates the operand
   * @throws IllegalStateException
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalStateException("Bad operand:" + _operand);
    }
  }

  /**
   * Returns this expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "COMPARISON(" + _left + "," + OPERAND_TEXT[_operand] + "," + _right + ")";
  }

  /**
   * Evauluate the comparison returning the result as a Boolean
   * @param context
   * @return result a Boolean
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      Object leftValue = _left.evaluate(context);
      Object rightValue = _right.evaluate(context);
      int c = BeanComparator.compareValues(leftValue, rightValue);
      switch (_operand){
        case NOT_EQUAL:
          return c != 0 ? Boolean.TRUE : Boolean.FALSE;
        case EQUAL:
          return c == 0 ? Boolean.TRUE : Boolean.FALSE;
        case GREATER_THAN:
          return c > 0 ? Boolean.TRUE : Boolean.FALSE;
        case GREATER_OR_EQUAL:
          return c >= 0 ? Boolean.TRUE : Boolean.FALSE;
        case LESS_THAN:
          return c < 0 ? Boolean.TRUE : Boolean.FALSE;
        case LESS_OR_EQUAL:
          return c <= 0 ? Boolean.TRUE : Boolean.FALSE;
        default:
          throw new IllegalStateException("Illegal operand:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
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
