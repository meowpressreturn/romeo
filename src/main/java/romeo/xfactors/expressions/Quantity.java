package romeo.xfactors.expressions;

import java.util.Locale;
import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;

/**
 * Implements the QUANTITY expression that returns the live quantity of a
 * specified unit. See the xfactor reference help in the resources folder for
 * details.
 */
public class Quantity implements IExpression {
  
  /**
   * Operand specifying to count the quantity in any fleet
   */
  public static final int ANY_PLAYER = 0;

  /**
   * Operand specifying to count the number owned by the player that owns the
   * xfactored unit.
   */
  public static final int THIS_PLAYER = 1;

  /**
   * Operand specifying to count the number owned by the player(s) that dont own
   * the xfactored unit.
   */
  public static final int OPPOSING_PLAYERS = 2;

  /**
   * Array that maps operand text to its int constant
   */
  private static final String[] OPERAND_TEXT = new String[3];
  static {
    OPERAND_TEXT[ANY_PLAYER] = "ANY_PLAYER";
    OPERAND_TEXT[THIS_PLAYER] = "THIS_PLAYER";
    OPERAND_TEXT[OPPOSING_PLAYERS] = "OPPOSING_PLAYERS";
  }
  
  public static int asOperand(String text) {
    String operandToken = Objects.requireNonNull(text,"operand text may not be null").toUpperCase(Locale.US);
    return Convert.toIndex(operandToken, OPERAND_TEXT);
  }
  
  /**
   * Utility method that counts the number of the specified unit within the
   * specified fleet and option subfleet as specified by sourceId. This doesnt
   * take casualties into account as it assumes this is the start of a round and
   * these have been removed.
   * @param fleet
   * @param acronym
   * @param sourceId may be null, in which case it is ignored and any element can be source
   * @return quantity
   */
  public static double getQuantity(FleetContents fleet, String acronym, Integer sourceId) {
    acronym = acronym.toUpperCase(Locale.US);
    double quantity = 0;
    for(FleetElement element : fleet) {
      String elementAcronym = element.getUnit().getAcronym();
      if(elementAcronym != null && acronym.equals(elementAcronym.toUpperCase(Locale.US))) {
        if(sourceId == null || element.getSource() == sourceId.intValue()) {
          quantity += element.getQuantity();
        }
      }
    }
    return quantity;
  }
  
  ////////////////////////////////////////////////////////////////////////////

  protected int _operand;
  protected String _acronym;
  protected Integer _sourceId;

  /**
   * Constructor
   * @param params
   *          params string
   * @param parser
   */
  public Quantity(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    try {
      String[] tokens = parser.tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      _operand = asOperand(tokens[0]);
      _acronym = tokens[1];
      //note: can be null (now case-insensive as of 0.6.3)
      if(tokens[2].equalsIgnoreCase("null")) {
        _sourceId = null;
      } else {
        _sourceId = new Integer(Integer.parseInt(tokens[2]));
      }
      validate();
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise QUANTITY with params:" + params, e);
    }
  }

  /**
   * Constructor. The sourceId is optional. If specified only the subfleet
   * specified will be counted.
   * @param player
   *          the index of this player
   * @param acronym
   *          the unit to count
   * @param sourceId
   *          this units source fleet among this players fleets
   */
  public Quantity(int player, String acronym, Integer sourceId) {
    _operand = player;
    _acronym = Objects.requireNonNull(acronym, "acronym may not be null");
    _sourceId = sourceId;
    validate();
  }

  /**
   * Validates the operand
   * @throws IllegalStateException
   */
  protected void validate() {
    if(_operand < 0 || _operand > OPERAND_TEXT.length) {
      throw new IllegalArgumentException("Bad player id:" + _operand);
    }
    if(_sourceId != null && _sourceId < 0) {
      throw new IllegalArgumentException("sourceId may not be negative");
    }
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "QUANTITY(" + OPERAND_TEXT[_operand] + "," + _acronym + "," + _sourceId + ")";
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
            quantity += getQuantity(fleets[i], _acronym, _sourceId);
          }
          quantity += getQuantity(context.getThisFleet(), _acronym, _sourceId);
          return new Double(quantity);
        }

        case THIS_PLAYER: {
          FleetContents fleet = context.getThisFleet();
          double quantity = getQuantity(fleet, _acronym, _sourceId);
          return new Double(quantity);
        }

        case OPPOSING_PLAYERS: {
          FleetContents[] fleets = context.getOpposingFleets();
          double quantity = 0;
          for(int i = 0; i < fleets.length; i++) {
            quantity += getQuantity(fleets[i], _acronym, _sourceId);
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

  public int getOperand() {
    return _operand;
  }
  
  public Integer getSourceId() {
    return _sourceId;
  }
  
  public boolean useSourceId() {
    return _sourceId != null;
  }
  
  public String getAcronym() {
    return _acronym;
  }
  
}
