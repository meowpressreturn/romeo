package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.BeanComparator;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;

/**
 * Implements the COMPARISON expression used to compare between values. See the
 * xfactor reference help in the resources folder for details.
 */
public class Comparison implements IExpression {
  
  public enum ComparisonOperand {
    
    NOT_EQUAL, EQUAL, GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL;
    
    public static ComparisonOperand fromString(String text) {
      String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(ComparisonOperand.class, operandToken);
    }
    
  }

  protected IExpression _left;
  protected ComparisonOperand _operand;
  protected IExpression _right;

  /**
   * Constructor that parses the params string
   * @param params
   * @param parser
   */
  public Comparison(String params, IExpressionParser parser, IExpressionTokeniser tokeniser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    Objects.requireNonNull(tokeniser, "tokeniser may not be null");
    try {
      String[] tokens = tokeniser.tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      _left = parser.getExpression(tokens[0]);
      _operand = ComparisonOperand.fromString(tokeniser.trimToken(tokens[1]));
      _right = parser.getExpression(tokens[2]);
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
  public Comparison(IExpression left, ComparisonOperand operand, IExpression right) {
    _left = Objects.requireNonNull(left, "left may not be null");
    _operand = Objects.requireNonNull(operand, "operand may not be null");
    _right = Objects.requireNonNull(right, "right may not be null");
  }

  /**
   * Returns this expression in XFEL
   * @return string
   */
  @Override
  public String toString() {
    return "COMPARISON(" + _left + "," + _operand + "," + _right + ")";
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

  public ComparisonOperand getOperand() {
    return _operand;
  }
  
  public IExpression getLeft() {
    return _left;
  }
  
  public IExpression getRight() {
    return _right;
  }
}
