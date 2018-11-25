package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.ExpressionFailure;
import romeo.xfactors.api.IExpression;

/**
 * Will cause an ExpressionFailure to be raised whose message comes from the value expression.
 * Immutable.
 */
public class Fail implements IExpression {
  
  private final IExpression _value;

  /**
   * Constructor
   * @param expression
   *          Expression that returns the message
   */
  public Fail(IExpression expression) {
    _value = Objects.requireNonNull(expression, "expression may not be null");
    //validate();
  }

  /**
   * Returns the expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "FAIL(" + _value + ")";
  }

  /**
   * Evaluate this expresssion throw an {@link ExpressionFailure}
   * @param context
   * @throw {@link ExpressionFailure}
   */
  @Override
  public Object evaluate(RoundContext context) {
    String value = null;
    try {
      value = "" + _value.evaluate(context);
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
    throw new ExpressionFailure(value, null);
  }
  
  public IExpression getExpression() {
    return _value;
  }

}
