/*
 * Context.java
 * Created on Mar 13, 2006
 */
package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;

/**
 * Implements the CONTEXT expression. This evaluates to one of a number of
 * contextual values. See the xfactor reference help in the resources folder for
 * details.
 */
public class Context implements IExpression {
  /**
   * Operand specifying to return the current round number
   */
  public static final int ROUND = 0;

  /**
   * Operand specifying to return true if this unit belongs to an attacker
   */
  public static final int IS_ATTACKER = 1;

  /**
   * Operand specifying to return true if this unit belongs to the defender
   */
  public static final int IS_DEFENDER = 2;

  /**
   * Operand specifying to return the units source fleet. This is a number
   * indicating which of the players fleets in this battle the unit belongs to.
   * For the defender 0 is the base fleet.
   */
  public static final int SOURCE = 3;

  /**
   * Operand specifying to return the units normal number of attacks
   */
  public static final int ATTACKS = 4;

  /**
   * Operand specifying to return the units normal offense
   */
  public static final int OFFENSE = 5;

  /**
   * Operand specifying to return the units normal defense
   */
  public static final int DEFENSE = 6;

  /**
   * Operand specifying to return true if the unit is in the base fleet
   */
  public static final int IS_BASE = 7;

  /**
   * Operand specifying to return true if the unit is not in the base fleet
   */
  public static final int IS_NOT_BASE = 8;

  /**
   * Operand specifying to return the units normal population damage
   */
  public static final int PD = 9;

  /**
   * Array that maps a units test to their constant int value which corresponds
   * with the arrays index.
   */
  public static final String[] OPERAND_TEXT = new String[10];
  static {
    OPERAND_TEXT[ROUND] = "ROUND";
    OPERAND_TEXT[IS_ATTACKER] = "IS_ATTACKER";
    OPERAND_TEXT[IS_DEFENDER] = "IS_DEFENDER";
    OPERAND_TEXT[SOURCE] = "SOURCE";
    OPERAND_TEXT[ATTACKS] = "ATTACKS";
    OPERAND_TEXT[OFFENSE] = "OFFENSE";
    OPERAND_TEXT[DEFENSE] = "DEFENSE";
    OPERAND_TEXT[IS_BASE] = "IS_BASE";
    OPERAND_TEXT[IS_NOT_BASE] = "IS_NOT_BASE";
    OPERAND_TEXT[PD] = "PD";
  }

  protected int _operand;

  /**
   * Constructor that parses the params
   * @param params
   * @param parser
   */
  public Context(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    try {
      String[] tokens = parser.tokenise(params);
      if(tokens.length != 1) {
        throw new IllegalArgumentException("Expecting 1 parameters but found " + tokens.length);
      }
      String operandToken = tokens[0].toUpperCase(Locale.US);
      _operand = Convert.toIndex(operandToken, OPERAND_TEXT);
      validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise CONTEXT with params:" + params, e);
    }
  }

  /**
   * Constructor
   * @param operand
   */
  public Context(int operand) {
    _operand = operand;
    validate();
  }

  /**
   * Validates that the operand in one of the valid constants
   * @throws IllegalStateException
   *           if it is not
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalArgumentException("Bad operand:" + _operand);
    }
  }

  /**
   * Returns the expression as an XFEL string
   * @retiurn string
   */
  @Override
  public String toString() {
    return "CONTEXT(" + OPERAND_TEXT[_operand] + ")";
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
          return new Integer(context.getRound());
        case IS_ATTACKER:
          return context.isAttacker() ? Boolean.TRUE : Boolean.FALSE;
        case IS_DEFENDER:
          return context.isDefender() ? Boolean.TRUE : Boolean.FALSE;
        case SOURCE:
          return new Integer(context.getFleetElement().getSource());
        case ATTACKS:
          return new Integer(context.getFleetElement().getUnit().getAttacks());
        case OFFENSE:
          return new Integer(context.getFleetElement().getUnit().getOffense());
        case DEFENSE:
          return new Integer(context.getFleetElement().getUnit().getDefense());
        case IS_BASE:
          return context.isDefender() && context.getFleetElement().getSource() == 0 ? Boolean.TRUE : Boolean.FALSE;
        case IS_NOT_BASE:
          return context.isDefender() && context.getFleetElement().getSource() == 0 ? Boolean.FALSE : Boolean.TRUE;
        case PD:
          return new Integer(context.getFleetElement().getUnit().getPd());
        default:
          throw new IllegalArgumentException("Illegal operand:" + _operand);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating " + toString(), e);
    }
  }
  
  public int getOperand() {
    return _operand;
  }
}
