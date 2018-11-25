package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

/**
 * Implements the IF expression which will return different results depending on
 * how the condition expression evaluates. See the xfactor reference help in the
 * resources folder for details.
 * Immutable.
 */
public class If implements IExpression {
  
  private final IExpression _condition;
  private final IExpression _trueResult;
  private final IExpression _falseResult;

  /**
   * Constructor
   * @param condition
   *          a condition that evaluates to true or false
   * @param trueResult
   *          expression whose value is returned if condition is true
   * @param falseResult
   *          expression whose value is returned if condition is false
   */
  public If(IExpression condition, IExpression trueResult, IExpression falseResult) {
    _condition = Objects.requireNonNull(condition, "condition may not be null");
    _trueResult = Objects.requireNonNull(trueResult, "trueResult may not be null");
    _falseResult = Objects.requireNonNull(falseResult, "falseResult may not be null");
  }

  /**
   * Returns this expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "IF(" + _condition + "," + _trueResult + "," + _falseResult + ")";
  }

  /**
   * Evaluate the condition and return the result of the appropriate result
   * expression
   * @param context
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      boolean condition = Logic.evalBool(_condition, context);
      return condition ? _trueResult.evaluate(context) : _falseResult.evaluate(context);
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public IExpression getCondition() {
    return _condition;
  }
  
  public IExpression getTrueResult() {
    return _trueResult;
  }
  
  public IExpression getFalseResult() {
    return _falseResult;
  }
}
