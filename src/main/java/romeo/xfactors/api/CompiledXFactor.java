/*
 * CompiledXFactor.java
 * Created on Mar 13, 2006
 */
package romeo.xfactors.api;

/**
 * Stores the IExpression trees used to evaluate the xfactors for the various
 * different stats of a unit that an xactor may modify. An expression is stored
 * for each of the unit stats that may be affected by an xfactor in Romeo, plus
 * a trigger expression that defines when the xfactor is operational.
 */
public class CompiledXFactor {
  protected IExpression _trigger;
  protected IExpression _xfAttacks;
  protected IExpression _xfOffense;
  protected IExpression _xfDefense;
  protected IExpression _xfPd;
  protected IExpression _xfRemove;

  /**
   * No-Args constructor
   */
  public CompiledXFactor() {
    ;
  }

  /**
   * Constructor
   * @param trigger
   * @param xfAttacks
   * @param xfOffense
   * @param xfDefense
   * @param xfPd
   * @param xfRemove
   */
  public CompiledXFactor(IExpression trigger,
                         IExpression xfAttacks,
                         IExpression xfOffense,
                         IExpression xfDefense,
                         IExpression xfPd,
                         IExpression xfRemove) {
    _trigger = trigger;
    _xfAttacks = xfAttacks;
    _xfOffense = xfOffense;
    _xfDefense = xfDefense;
    _xfPd = xfPd;
    _xfRemove = xfRemove;
  }

  /**
   * Returns a string displaying the expressions for each of the stats, suitable
   * for debugging
   * @return string
   */
  @Override
  public String toString() {
    return "X-Factor:\ntrigger=" + _trigger + "\nattacks=" + _xfAttacks + "\nOffense=" + _xfOffense + "\ndefense="
        + _xfDefense + "\npd=" + _xfPd + "\nremove=" + _xfRemove;
  }

  /**
   * Returns the expression used to evaluate if the xfactors are operational
   * @return trigger
   */
  public IExpression getTrigger() {
    return _trigger;
  }

  /**
   * Returns expression defining the number of attacks the unit gets
   * @return xfAttacks vor null if using default values
   */
  public IExpression getXfAttacks() {
    return _xfAttacks;
  }

  /**
   * Returns the expression defining the units defense
   * @return xfDefense or null if using defaults
   */
  public IExpression getXfDefense() {
    return _xfDefense;
  }

  /**
   * Returns the expression that defines the units offense
   * @return xfOffense or null if using default offense
   */
  public IExpression getXfOffense() {
    return _xfOffense;
  }

  /**
   * Returns the expression defining the population damage
   * @return xfPd or null if using default
   */
  public IExpression getXfPd() {
    return _xfPd;
  }

  /**
   * Returns the expression that defines when the unit gets removed early from
   * combat
   * @return xfRemove or null
   */
  public IExpression getXfRemove() {
    return _xfRemove;
  }

  /**
   * Set the trigger expression
   * @param expression
   */
  public void setTrigger(IExpression expression) {
    _trigger = expression;
  }

  /**
   * Set the expression defining the attacks
   * @param expression
   */
  public void setXfAttacks(IExpression expression) {
    _xfAttacks = expression;
  }

  /**
   * Set the expression defining the defense
   * @param expression
   */
  public void setXfDefense(IExpression expression) {
    _xfDefense = expression;
  }

  /**
   * Set the expression defining the offense
   * @param expression
   */
  public void setXfOffense(IExpression expression) {
    _xfOffense = expression;
  }

  /**
   * Set expression defining the population damage
   * @param expression
   */
  public void setXfPd(IExpression expression) {
    _xfPd = expression;
  }

  /**
   * Set the expression defining early unit removal from combat
   * @param xfRemove
   */
  public void setXfRemove(IExpression expression) {
    _xfRemove = expression;
  }

}
