/*
 * Logic.java
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
 * Implements the LOGIC expression which allows one to perform various logic
 * acts on the results of subexpressions. See the xfactor reference help in the
 * resources folder for details.
 */
public class Logic implements IExpression {
  /**
   * The AND operand. Tests that both values are true.
   */
  public static final int AND = 0;

  /**
   * The OR operand. Tests that one or both values are true.
   */
  public static final int OR = 1;

  /**
   * The Exclusive-OR operand. Tests that either one value or the other is true but not both.
   */
  public static final int XOR = 2;

  /**
   * The neither or operand. Tests that both values are false.
   */
  public static final int NOR = 3;

  /**
   * The NOT operand (short for not equals). Tests that the two values are not the same.
   */
  public static final int NOT = 4;

  /**
   * The EQUAL operand. Tests that the values are the same.
   */
  public static final int EQUAL = 5;

  /**
   * Array mapping operands to their constant int representation
   */
  public static final String[] OPERAND_TEXT = new String[6];
  static {
    OPERAND_TEXT[AND]   = "AND";
    OPERAND_TEXT[OR]    = "OR";
    OPERAND_TEXT[XOR]   = "XOR";
    OPERAND_TEXT[NOR]   = "NOR";
    OPERAND_TEXT[NOT]   = "NOT";
    OPERAND_TEXT[EQUAL] = "EQUAL";
  }
  
  /**
   * Converts the expressions result to a Boolean applying various heuristics
   * for non boolean result types. (Null is considered false, as is 0, a string
   * must be "TRUE" to be true or else it too is false).
   * @param expression
   * @param context
   * @return bool
   */
  public static boolean evalBool(IExpression expression, RoundContext context) {
    Objects.requireNonNull(expression, "expression may not be null");
    Objects.requireNonNull(context, "context may not be null");
    Object value = null;
    try {
      value = expression.evaluate(context);
    } catch(Exception e) {
      throw new RuntimeException("Failed to evaluate expression:" + expression, e);
    }
    if(value == null)
      return false;
    if(value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    } else if(value instanceof Number) {
      return ((Number) value).doubleValue() != 0;
    } else if(value instanceof String) {
      return "TRUE".equalsIgnoreCase((String)value);
    } else {
      throw new IllegalArgumentException("Unable to convert value to boolean:" + value);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////

  protected IExpression _left;
  protected IExpression _right;
  protected int _operand;

  /**
   * Constructor that parses the params
   * @param params
   * @param parser
   */
  public Logic(String params, IExpressionParser parser) {
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
      throw new RuntimeException("Unable to initialise LOGIC with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param left
   *          the left expression
   * @param operand
   * @param the
   *          right expression
   */
  public Logic(IExpression left, int operand, IExpression right) {
    _left = Objects.requireNonNull(left, "left may not be null");
    _right = Objects.requireNonNull(right, "right may not be null");
    _operand = operand;
    validate();
  }

  /**
   * Validates that the operand is one of the valid constants
   * @throws IllegalStateException
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalArgumentException("Bad operand:" + _operand);
    }
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "LOGIC(" + _left + "," + OPERAND_TEXT[_operand] + "," + _right + ")";
  }

  /**
   * Evaluates the expression
   * @param context
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      boolean leftValue = evalBool(_left, context);
      boolean rightValue = evalBool(_right, context);
      switch (_operand){
        case AND:
          return leftValue && rightValue ? Boolean.TRUE : Boolean.FALSE;
        case OR:
          return leftValue || rightValue ? Boolean.TRUE : Boolean.FALSE;
        case XOR:
          return (!(leftValue && rightValue) && (leftValue || rightValue)) ? Boolean.TRUE : Boolean.FALSE;
        case NOR:
          return (leftValue == false) && (rightValue == false) ? Boolean.TRUE : Boolean.FALSE;
        case NOT:
          return leftValue != rightValue ? Boolean.TRUE : Boolean.FALSE;
        case EQUAL:
          return leftValue == rightValue ? Boolean.TRUE : Boolean.FALSE;
        default:
          throw new IllegalStateException("Illegal operand:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public IExpression getLeft() {
    return _left;
  }
  
  public IExpression getRight() {
    return _right;
  }
  
  public int getOperand() {
    return _operand;
  }

}
