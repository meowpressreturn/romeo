package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

/**
 * Implements the LOGIC expression which allows one to perform various logic
 * acts on the results of subexpressions. See the xfactor reference help in the
 * resources folder for details.
 */
public class Logic implements IExpression {
  
  public enum LogicOperand {
    /**
     * The AND operand. Tests that both values are true.
     */
    AND,
  
    /**
     * The OR operand. Tests that one or both values are true.
     */
    OR,
  
    /**
     * The Exclusive-OR operand. Tests that either one value or the other is true but not both.
     */
    XOR,
  
    /**
     * The neither or operand. Tests that both values are false.
     */
    NOR,
  
    /**
     * The NOT operand (short for not equals). Tests that the two values are not the same.
     */
    NOT,
  
    /**
     * The EQUAL operand. Tests that the values are the same.
     */
    EQUAL;
    
    public static LogicOperand fromString(String text) {
      String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(LogicOperand.class, operandToken);
    }
    
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
  protected LogicOperand _operand;

  /**
   * Constructor
   * @param left
   *          the left expression
   * @param operand
   * @param the
   *          right expression
   */
  public Logic(IExpression left, LogicOperand operand, IExpression right) {
    _left = Objects.requireNonNull(left, "left may not be null");
    _operand = Objects.requireNonNull(operand, "operand may not be null");
    _right = Objects.requireNonNull(right, "right may not be null");
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "LOGIC(" + _left + "," + _operand + "," + _right + ")";
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
  
  public LogicOperand getOperand() {
    return _operand;
  }

}
