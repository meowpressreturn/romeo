package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.xfactors.api.IExpression;

/**
 * Expression that returns true if a specified flag is found. It requires that
 * the player to be checked is specified and the name of the flag to find. If
 * the flag is found then true is returned but if not then false will be
 * returned. (ie: FLAG(THIS_PLAYER,"O").
 * Immutable.
 */
public class Flag implements IExpression {
  
  public enum FlagOperand {
    
    /**
     * Operand specifying to find the flag in any fleet
     */
    ANY_PLAYER,
  
    /**
     * Operand specifying to check the flag for the player that owns the xfactored
     * unit.
     */
    THIS_PLAYER,
  
    /**
     * Operand specifying to check the flag for player(s) that don't own the
     * xfactored unit.
     */
    OPPOSING_PLAYERS;
  
    public static FlagOperand fromString(String text) {
      String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(FlagOperand.class, operandToken);
    }
  }

  private final FlagOperand _operand;
  private final IExpression _flag;

  /**
   * Constructor.
   * @param operand an operand indicating which fleet(s) to check for flag
   * @param flag
   *          expression returning flag text that will be checked for
   */
  public Flag(FlagOperand operand, IExpression flag) {
    _operand = Objects.requireNonNull(operand, "operand may not be null");
    _flag = Objects.requireNonNull(flag, "flag may not be null");
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "FLAG(" + _operand + "," + _flag + ")";
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
  
  public FlagOperand getOperand() {
    return _operand;
  }
  
  public IExpression getFlag() {
    return _flag;
  }

}
