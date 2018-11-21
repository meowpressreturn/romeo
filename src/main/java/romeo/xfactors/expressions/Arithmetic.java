package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;

/**
 * Implements the ARITHMETIC expression. This is used to perform various simple
 * maths operations. See the xfactor reference help in the resources folder for
 * details.
 */
public class Arithmetic implements IExpression {
  
  /**
   * Converts the result of the expression to a primitive double
   * @param expression
   * @param context
   * @return result
   */
  public static double evalDouble(IExpression expression, RoundContext context) {
    Objects.requireNonNull(expression, "expression may not be null");
    Objects.requireNonNull(context, "context may not be null");
    Object value = expression.evaluate(context);
    return Convert.toDouble(value);
  }
  
  public enum ArithmeticOperand {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MIN, MAX, ROOT, POWER;
    
    public static ArithmeticOperand fromString(String text) {
      String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(ArithmeticOperand.class, operandToken);
    }
    
  }

  protected ArithmeticOperand _operand;
  protected IExpression _left;
  protected IExpression _right;

  /**
   * Takes the expression parameter string and parses it using the supplied
   * parser to initialise this expression object.
   * @param params
   * @param parser
   */
  public Arithmetic(String params, IExpressionParser parser, IExpressionTokeniser tokeniser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    Objects.requireNonNull(tokeniser, "tokeniser may not be null");
    try {
      String[] tokens = tokeniser.tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      _left = parser.getExpression(tokens[0]);
      _operand = ArithmeticOperand.fromString( tokeniser.trimToken(tokens[1]));
      _right = parser.getExpression(tokens[2]);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise ARITHMETIC with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param left
   *          the left expression
   * @param operand
   * @param right
   *          the right expression
   */
  public Arithmetic(IExpression left, ArithmeticOperand operand, IExpression right) {
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
    return "ARITHMETIC(" + _left + "," + _operand + "," + _right + ")";
  }

  /**
   * Evaulates this expression and returns the result
   * @param context
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      double left = evalDouble(_left, context);
      double right = evalDouble(_right, context);
      switch (_operand){
        case ADD:
          return new Double(left + right);
        case SUBTRACT:
          return new Double(left - right);
        case MULTIPLY:
          return new Double(left * right);
        case DIVIDE:
          return new Double(left / right);
        case MIN:
          return left < right ? new Double(left) : new Double(right);
        case MAX:
          return left > right ? new Double(left) : new Double(right);
        case ROOT:
          if(right != 2d) {
            throw new UnsupportedOperationException("Only square root supported currently");
          }
          return new Double(Math.sqrt(left));
        case POWER:
          return new Double(Math.pow(left, right));
        default:
          throw new IllegalStateException("Illegal operand:" + _operand);
      }
    } catch(UnsupportedOperationException unsup) {
      throw unsup;
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public ArithmeticOperand getOperand() {
    return _operand;
  }
  
  public IExpression getLeft() {
    return _left;
  }
  
  public IExpression getRight() {
    return _right;
  }
}
