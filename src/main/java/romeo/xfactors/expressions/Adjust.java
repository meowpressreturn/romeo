package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

/**
 * Implements the ADJUST expression. This is used to round values. See the
 * xfactor reference help in the resources folder for details.
 * Immutable.
 */
public class Adjust implements IExpression {
  
  public enum AdjustOperand {
    
    /**
     * Round the value to the nearest integer
     */
    ROUND, 
    
    /**
     * Round down to the nearest integer less than the value
     */
    FLOOR, 
    
    /**
     * Round up the value to the nearest integer greater than the value
     */
    CEILING;
    
    /**
     * Create an {@link AdjustOperand} from the specified text in a case-insensitive manner.
     * nb: does not perform trimming of whitespace
     * @param text
     * @return operand
     */
    public static AdjustOperand fromString(final String text) {
      final String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(AdjustOperand.class, operandToken);
    }
    
  }

  private final IExpression _value;
  private final AdjustOperand _operand;

  /**
   * Constructor
   * @param valueExpression
   *          Expression that returns the value to be adjusted
   * @param operand
   *          ajustment operation to be performed on the value to give return
   *          value
   */
  public Adjust(IExpression valueExpression, AdjustOperand operand) {
    _value = Objects.requireNonNull(valueExpression, "valueExpression may not be null");
    _operand = Objects.requireNonNull(operand, "operand may not be null");
  }

  /**
   * Returns the expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "ADJUST(" + _value + "," + _operand + ")";
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
  
  public AdjustOperand getOperand() {
    return _operand;
  }
  
  public IExpression getValue() {
    return _value;
  }

}
