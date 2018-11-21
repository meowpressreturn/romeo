package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

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

  protected IExpression _value;
  protected int _operand;

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
