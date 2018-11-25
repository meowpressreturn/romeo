package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

/**
 * Implements the CONTEXT expression. This evaluates to one of a number of
 * contextual values. See the xfactor reference help in the resources folder for
 * details.
 * Immutable.
 */
public class Context implements IExpression {
  
  public enum ContextOperand {
    
    /**
     * Operand specifying to return the current round number
     */
    ROUND,
  
    /**
     * Operand specifying to return true if this unit belongs to an attacker
     */
    IS_ATTACKER,
  
    /**
     * Operand specifying to return true if this unit belongs to the defender
     */
    IS_DEFENDER,
  
    /**
     * Operand specifying to return the unit's source fleet. This is a number
     * indicating which of the player's fleets in this battle the unit belongs to.
     * For the defender 0 is the base fleet.
     */
    SOURCE,
  
    /**
     * Operand specifying to return the unit's normal number of attacks
     */
    ATTACKS,
  
    /**
     * Operand specifying to return the unit's normal offense
     */
    OFFENSE,
  
    /**
     * Operand specifying to return the unit's normal defense
     */
    DEFENSE,
  
    /**
     * Operand specifying to return true if the unit is in the base fleet
     */
    IS_BASE,
  
    /**
     * Operand specifying to return true if the unit is not in the base fleet
     */
    IS_NOT_BASE,
  
    /**
     * Operand specifying to return the unit's normal population damage
     */
    PD;
    
    public static ContextOperand fromString(String text) {
      String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
      return valueOf(ContextOperand.class, operandToken);
    }
    
  }

  private final ContextOperand _operand;

  /**
   * Constructor
   * @param operand
   */
  public Context(ContextOperand operand) {
    _operand = Objects.requireNonNull(operand, "operand may not be null");
  }

  /**
   * Returns the expression as an XFEL string
   * @retiurn string
   */
  @Override
  public String toString() {
    return "CONTEXT(" + _operand + ")";
  }

  /**
   * Evaluate the expression to return the appropriate value
   * @param context
   * @return value
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      switch (_operand){
        case ROUND:
          return context.getRound();
        case IS_ATTACKER:
          return context.isAttacker();
        case IS_DEFENDER:
          return context.isDefender();
        case SOURCE:
          return context.getFleetElement().getSource(); //nb: this is now a SourceId and not an Integer
        case ATTACKS:
          return context.getFleetElement().getUnit().getAttacks();
        case OFFENSE:
          return context.getFleetElement().getUnit().getOffense();
        case DEFENSE:
          return context.getFleetElement().getUnit().getDefense();
        case IS_BASE:
          return (context.isDefender() && context.getFleetElement().getSource().isBaseOrDefault());
        case IS_NOT_BASE:
          return !(context.isDefender() && context.getFleetElement().getSource().isBaseOrDefault());
        case PD:
          return context.getFleetElement().getUnit().getPd();
        default:
          throw new IllegalArgumentException("Illegal operand:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public ContextOperand getOperand() {
    return _operand;
  }
}
