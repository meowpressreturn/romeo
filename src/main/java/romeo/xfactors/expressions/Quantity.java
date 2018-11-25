package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.SourceId;
import romeo.xfactors.api.IExpression;

/**
 * Implements the QUANTITY expression that returns the live quantity of a
 * specified unit. See the xfactor reference help in the resources folder for
 * details.
 * Immutable.
 */
public class Quantity implements IExpression {
  
  public enum QuantityOperand {
  
    /**
     * Operand specifying to count the quantity in any fleet
     */
    ANY_PLAYER,
  
    /**
     * Operand specifying to count the number owned by the player that owns the
     * xfactored unit.
     */
    THIS_PLAYER,
  
    /**
     * Operand specifying to count the number owned by the player(s) that dont own
     * the xfactored unit.
     */
    OPPOSING_PLAYERS;
    
    public static QuantityOperand fromString(String text) {
      String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(QuantityOperand.class, operandToken);
    }
  
  }
  
  ////////////////////////////////////////////////////////////////////////////

  private final QuantityOperand _operand;
  private final String _acronym;
  private final SourceId _sourceId;

  /**
   * Constructor. The sourceId is optional. If specified only the subfleet
   * specified will be counted.
   * @param operand
   *          the index of this player
   * @param acronym
   *          the unit to count
   * @param sourceId
   *          this unit's source fleet among this players fleets (for a defending player 0 is the base fleet)
   */
  public Quantity(QuantityOperand operand, String acronym, SourceId sourceId) {
    _operand = Objects.requireNonNull(operand, "operand may not be null");
    _acronym = Objects.requireNonNull(acronym, "acronym may not be null");
    _sourceId = Objects.requireNonNull(sourceId, "sourceId may not be null");
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "QUANTITY(" + _operand + "," + _acronym + "," + _sourceId + ")";
  }

  /**
   * Evaluate the expression
   * @param context
   * @return quantity
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      switch (_operand){
        case ANY_PLAYER: {
          FleetContents[] fleets = context.getOpposingFleets();
          double quantity = 0;
          for(int i = 0; i < fleets.length; i++) {
            quantity += fleets[i].getQuantity(_acronym, _sourceId);
          }
          quantity += context.getThisFleet().getQuantity(_acronym, _sourceId);
          return new Double(quantity);
        }

        case THIS_PLAYER: {
          FleetContents fleet = context.getThisFleet();
          double quantity = fleet.getQuantity(_acronym, _sourceId);
          return new Double(quantity);
        }

        case OPPOSING_PLAYERS: {
          FleetContents[] fleets = context.getOpposingFleets();
          double quantity = 0;
          for(int i = 0; i < fleets.length; i++) {
            quantity += fleets[i].getQuantity(_acronym, _sourceId);
          }
          return new Double(quantity);
        }

        default:
          throw new IllegalArgumentException("Illegal player:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }

  public QuantityOperand getOperand() {
    return _operand;
  }
  
  public SourceId getSourceId() {
    return _sourceId;
  }
  
  public String getAcronym() {
    return _acronym;
  }
  
}
