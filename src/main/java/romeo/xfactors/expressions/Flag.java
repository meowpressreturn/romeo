package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;

/**
 * Expression that returns true if a specified flag is found. It requires that
 * the player to be checked is specified and the name of the flag to find. If
 * the flag is found then true is returned but if not then false will be
 * returned. (ie: FLAG(THIS_PLAYER,"O").
 */
public class Flag implements IExpression {
  /**
   * Operand specifying to find the flag in any fleet
   */
  public static final int ANY_PLAYER = 0;

  /**
   * Operand specifying to check the flag for the player that owns the xfactored
   * unit.
   */
  public static final int THIS_PLAYER = 1;

  /**
   * Operand specifying to check the flag for player(s) that dont own the
   * xfactored unit.
   */
  public static final int OPPOSING_PLAYERS = 2;

  /**
   * Array that maps operand text to its int constant
   */
  public static final String[] OPERAND_TEXT = new String[3];
  static {
    OPERAND_TEXT[ANY_PLAYER] = "ANY_PLAYER";
    OPERAND_TEXT[THIS_PLAYER] = "THIS_PLAYER";
    OPERAND_TEXT[OPPOSING_PLAYERS] = "OPPOSING_PLAYERS";
  }
  
  public static int asOperand(String text) {
    String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
    return Convert.toIndex(operandToken, OPERAND_TEXT);
  }

  protected int _operand;
  protected IExpression _flag;

  /**
   * Constructor
   * @param params
   *          params string
   * @param parser
   */
  public Flag(String params, IExpressionParser parser, IExpressionTokeniser tokeniser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    Objects.requireNonNull(tokeniser, "tokeniser may not be null");
    try {
      String[] tokens = tokeniser.tokenise(params);
      if(tokens.length != 2) {
        throw new IllegalArgumentException("Expecting 2 parameters but found " + tokens.length);
      }
      _operand = asOperand(tokens[0]);
      _flag = parser.getExpression(tokens[1]);
      validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise FLAG with params:" + params, e);
    }
  }

  /**
   * Constructor.
   * @param operand an operand indicating which fleet(s) to check for flag
   * @param flag
   *          expression returning flag text that will be checked for
   */
  public Flag(int operand, IExpression flag) {
    _operand = operand;
    _flag = Objects.requireNonNull(flag, "flag may not be null");
    validate();
  }

  /**
   * Validates the operand
   * @throws IllegalStateException
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalArgumentException("invalid operand:" + _operand);
    }
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "FLAG(" + OPERAND_TEXT[_operand] + "," + _flag + ")";
  }

  /**
   * Evaluate the expression
   * @param context
   * @return hasFlag a Boolean
   */
  @Override
  public Object evaluate(RoundContext context) {
    Object flagObject = _flag.evaluate(context);
    if(flagObject == null) {
      return Boolean.FALSE;
    }
    //Convert flag to text
    String flag = flagObject.toString().toUpperCase();
    try {
      switch (_operand){
        case ANY_PLAYER: {
          FleetContents[] fleets = context.getOpposingFleets();
          for(int i = 0; i < fleets.length; i++) { //Check each enemy fleet for the flag
            if(fleets[i].hasFlag(flag)) {
              return Boolean.TRUE;
            }
          }
          //If not found in an enemy fleet finally check this fleet
          return new Boolean(context.getThisFleet().hasFlag(flag));
        }

        case THIS_PLAYER: {
          return new Boolean(context.getThisFleet().hasFlag(flag));
        }

        case OPPOSING_PLAYERS: {
          FleetContents[] fleets = context.getOpposingFleets();
          for(int i = 0; i < fleets.length; i++) { //Check each enemy fleet for the flag
            if(fleets[i].hasFlag(flag)) {
              return Boolean.TRUE;
            }
          }
          //Return false if none of them had the flag
          return Boolean.FALSE;
        }

        default:
          throw new IllegalArgumentException("Illegal player:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public int getOperand() {
    return _operand;
  }
  
  public IExpression getFlag() {
    return _flag;
  }

}
