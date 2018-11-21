package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;

/**
 * Implements the IF expression which will return different results depending on
 * how the condition expression evaluates. See the xfactor reference help in the
 * resources folder for details.
 */
public class If implements IExpression {
  
  protected IExpression _condition;
  protected IExpression _trueResult;
  protected IExpression _falseResult;

  /**
   * Constructor that parses the params
   * @param params
   * @param parser
   */
  public If(String params, IExpressionParser parser, IExpressionTokeniser tokeniser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");      
    Objects.requireNonNull(tokeniser, "tokeniser may not be null");
    try {
      String[] tokens = tokeniser.tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      _condition = parser.getExpression(tokens[0]);
      _trueResult = parser.getExpression(tokens[1]);
      _falseResult = parser.getExpression(tokens[2]);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise IF with params:" + params, e);
    }
  }

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
