package romeo.xfactors.expressions;

import java.util.Objects;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;

/**
 * Implements the PRESENT expression. This checks for the presence of one of
 * more of a specific type of unit as specified by its acronym in the same fleet
 * as the xfactored unit. See the xfactor reference help in the resources folder
 * for details.
 */
public class Present implements IExpression {
  
  protected String _acronym;

  /**
   * Constructor. nb: the string supplied will not be trimmed here.
   * @param acronym 
   */
  public Present(String acronym) {
    _acronym = Objects.requireNonNull(acronym);
    if(_acronym.isEmpty()) {
      throw new IllegalArgumentException("acronym not specified");
    }
    //TODO - verify there are no illegal chars in the acronym
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
      boolean present = context.getThisFleet().unitPresent(_acronym);
      return present ? Boolean.TRUE : Boolean.FALSE;
    } catch(Exception e) {
      throw new RuntimeException("Error evaluating unit presence for " + _acronym, e);
    }
  }
  
  public String getAcronym() {
    return _acronym;
  }
}
