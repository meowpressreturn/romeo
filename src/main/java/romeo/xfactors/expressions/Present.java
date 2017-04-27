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
 * Implements the PRESENT expression. This checks for the presence of one of
 * more of a specific type of unit as specified by its acronym in the same fleet
 * as the xfactored unit. See the xfactor reference help in the resources folder
 * for details.
 */
public class Present implements IExpression {
  
  /**
   * Utility method that will search the specified fleet for live units with the
   * specified acronym
   * @param fleet
   * @param acronym
   * @return present
   */
  public static boolean unitPresent(FleetContents fleet, String acronym) {
    Objects.requireNonNull(fleet, "fleet may not be null");
    Objects.requireNonNull(acronym, "acronym may not be null");
    acronym = acronym.toUpperCase(Locale.US);
    for(FleetElement element : fleet) {
      String elementAcronym = element.getUnit().getAcronym();
      if(elementAcronym != null && acronym.equals(elementAcronym.toUpperCase())) {
        if(element.getQuantity() > 0) {
          return true;
        }
      }
    }
    return false;
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  
  protected String _acronym;

  /**
   * Constructor that takes the acronym as a params string. This is de-tokenised
   * and trimmed. The value may be quoted.
   * @param params 
   * @param parser
   * 
   */
  public Present(String params, IExpressionParser parser) {
    Objects.requireNonNull(params, "params may not be null");
    Objects.requireNonNull(parser, "parser may not be null");
    String[] tokens = parser.tokenise(params);
    if(tokens.length != 1) {
      throw new IllegalArgumentException("expected a single acronym");
    }
    _acronym = parser.trimToken(tokens[0]);
    _acronym = Convert.toUnquotedString(_acronym);
    validate();
  }

  /**
   * Constructor. nb: the string supplied will not be trimmed.
   * @param acronym 
   */
  public Present(String acronym) {
    _acronym = acronym;
    validate();
  }

  /**
   * Validates that acronym is not null or an empty string
   * @throws IllegalStateException
   *           if it is
   */
  protected void validate() {
    Objects.requireNonNull(_acronym,"acronym may not be null");
    if(_acronym.isEmpty()) {
      throw new IllegalArgumentException("acronym not specified");
    }
  }

  /**
   * Returns this expression as an XFEL string
   * @return string
   */
  @Override
  public String toString() {
    return "PRESENT(" + _acronym + ")";
  }

  /**
   * Returns True if the specified unit is present in this fleet or false
   * otherwise
   * @param context
   * @return present
   */
  @Override
  public Object evaluate(RoundContext context) {
    try {
      boolean present = unitPresent(context.getThisFleet(), _acronym);
      return present ? Boolean.TRUE : Boolean.FALSE;
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating unit presence for " + _acronym, e);
    }
  }
  
  public String getAcronym() {
    return _acronym;
  }
}
