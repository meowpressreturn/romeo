//2008-12-09
package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.ExpressionFailure;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;

/**
 * Will cause an ExpressionFailure to be raised
 */
public class Fail implements IExpression {
  
  protected IExpression _value;

  /**
   * Constructor that parses the string parameters to initialise this expression
   * object
   * @param params
   *          the parameters to the FAIL expression
   * @param parser
   *          the XFEL parser
   */
  public Fail(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    try {
      String[] tokens = parser.tokenise(params);
      if(tokens.length != 1) {
        throw new IllegalArgumentException("Expecting 1 parameter but found " + tokens.length);
      }
      _value = parser.getExpression(tokens[0]);
      //validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise Fail with params:" + params, e);
    }
  }

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
