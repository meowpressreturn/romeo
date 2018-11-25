package romeo.fleet.model;

import java.util.Objects;
import java.util.Random;

import romeo.battle.impl.RoundContext;
import romeo.units.api.IUnit;
import romeo.units.impl.UnitImpl;
import romeo.xfactors.api.CompiledXFactor;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.XFactorId;
import romeo.xfactors.expressions.Arithmetic;
import romeo.xfactors.expressions.Logic;

/**
 * Mutable structure used within a {@link FleetContents} to maintain information about
 * quantities of a particular type of unit and its stats after any xFactors have
 * been applied.
 * Note: elements have a fleet reference that gets set when you add them to a fleet, so they
 * may not be added to multiple fleets.
 */
public class FleetElement implements Cloneable, Comparable<FleetElement> {
  private Random _rnd = new Random();

  private IUnit _unit;
  private double _quantity;
  private int _casualties;
  private SourceId _source;
  private FleetContents _fleet;

  private CompiledXFactor _xFactor;
  private boolean _xfActive;
  private int _computedAttacks;
  private int _computedOffense;
  private int _computedDefense;
  private int _computedPd;

  /**
   * Initialise the element with specified values. See the docs for the
   * individual getters and setters for more details
   * @param unit
   *          The unit type
   * @param qty
   *          The number of units in the element
   * @param source
   *          The origin fleet identifier
   */
  public FleetElement(IUnit unit, double qty, SourceId source) {
    setUnit(unit);
    setQuantity(qty);
    setSource(source);
  }

  /**
   * Use the compiler to parse the xfactor expression text and return an object
   * tree representation of the xfactor that contains the logic to perform the
   * evaluation. The compiled xfactor is then stored in the element as the
   * xfactor property.
   * @param compiler
   */
  public void compileXFactor(IXFactorCompiler compiler) {
    XFactorId xfId = getUnit().getXFactor();
    if(xfId != null) {
      _xFactor = compiler.getXFactor(xfId);
    }
  }

  /**
   * Evaluate the (already compiled) xfactor using the supplied round context
   * and update the computed properties for unit stats. If the xfactor is not
   * active then the computed stats will be taken from the stats in the Unit
   * object.
   * @param roundContext
   *          Provides access to contextual information used by the xf logic
   * @return isActive true if the xfactor is active this round
   */
  public boolean evaluateXFactor(RoundContext context) {
    IUnit unit = getUnit();
    _xfActive = false;

    //System.out.println("xFactor=" + _xFactor);

    if(_xFactor != null) {
      if(_quantity > 0 && _xFactor.getTrigger() != null) {
        context.setFleetElement(this);
        _xfActive = Logic.evalBool(_xFactor.getTrigger(), context);
      }

      IExpression xfRemove = _xFactor.getXfRemove();
      boolean removeUnits = xfRemove != null ? Logic.evalBool(xfRemove, context) : false;
      //System.out.println(unit + " - - round " + context.getRound() + ", removeUnits?=" + removeUnits);
      if(removeUnits) {
        //System.out.println(unit + " - - round " + context.getRound() + ", remove " + _quantity);
        setCasualties((int) _quantity);
      }

      //Now compute appropriate stats to use for this element this round
      if(_xfActive) {
        _computedAttacks = evalStat(context, unit.getAttacks(), _xFactor.getXfAttacks());
        _computedOffense = evalStat(context, unit.getOffense(), _xFactor.getXfOffense());
        _computedDefense = evalStat(context, unit.getDefense(), _xFactor.getXfDefense());
        _computedPd = evalStat(context, unit.getPd(), _xFactor.getXfPd());
      }
    }

    if(_xfActive == false) { //No xFactor means we use the normal stats
      _computedAttacks = unit.getAttacks();
      _computedOffense = unit.getOffense();
      _computedDefense = unit.getDefense();
      _computedPd = unit.getPd();
      if(_computedPd < 0) {
        _computedPd = 0;
      }
    }

    //Restrict the statistics to a 0..100 range. Its debatable whether we should
    //do this or throw an error, however I think this is good enough for Romeo for now
    //and an error would need more plumbing work to display it. (attacks dont have an upper
    //bound but we limit attacks for now as anyhting like that high is likely an error already
    //and each attack does involve work...)
    _computedAttacks = boundStat(_computedAttacks, 0, 99999);
    _computedOffense = boundStat(_computedOffense);
    _computedDefense = boundStat(_computedDefense);

    return _xfActive;
  }

  /**
   * Ensure that a stat is in the range 0..100 inclusive. If it isnt then that
   * probably means theres a logic error in the expression the user provided for
   * the xfactor, however we kludge the stat into suitable bounds to avoid
   * nastier logic errors in our own code that we cant be bothered handling
   * better. Perhaps we should let it bomb out now that we have 'better' error
   * reporting? Shall leave it like this for this version.
   * @param stat
   * @param boundedStat
   */
  private int boundStat(int stat) {
    return boundStat(stat, 0, 100);
  }

  /**
   * Returns min or max if stat is outside that range, or stat otherwise
   * @param stat
   * @param min
   * @param max
   * @return boundedStat
   */
  private int boundStat(int stat, int min, int max) {
    if(stat > max)
      stat = max;
    if(stat < min)
      stat = min;
    return stat;
  }

  /**
   * If expr is null just uses the normal unit stat. Otherwise it evaluates the
   * expression and uses the floor of that as the stat.
   * @param context
   * @param normal
   *          The normal value for this stat
   * @param expr
   *          The expression giving the new value
   */
  private int evalStat(RoundContext context, int normal, IExpression expr) {
    if(expr == null) {
      return normal;
    } else {
      double value = Arithmetic.evalDouble(expr, context);
      //System.out.println("Result=" + value + " for " + expr);
      return (int) Math.floor(value);
    }
  }

  /**
   * Returns a clone of this element using the default java cloning
   * functionality Fleet ref in clone is null
   * @return clone
   */
  @Override
  public Object clone() {
    try {
      FleetElement clone = (FleetElement) super.clone();
      clone.setFleet(null);
      return clone;
    } catch(CloneNotSupportedException arrgh) {
      throw new RuntimeException("Failed to clone FleetElement", arrgh);
    }
  }

  /**
   * Add contents of the specified element to this one - if it is a
   * FleetElement. If not it will be refused. This impl will convert the result
   * to zero if it is negative. Subclass that override can allow a diparate
   * class addition.
   * Since 0.3.1 the addition is refused if sourceId differs. 
   * @param element
   *          Element whose contents are to be added to this
   * @return allowed False if this is not a legal addition
   */
  public boolean add(FleetElement element) {
    if(element.getClass() != this.getClass()) {
      //Different subclasses presumed to store extra info we dont
      //want to lose so would need to stay in own element in a fleets
      //contents.
      return false;
    }
    //yeah, okay so weve never used the subclass stuff, but the following
    //we do use now in 0.3.1
    if(!element.getSource().equals(getSource())) {
      return false; //Reject it as it belongs to a different fleet under this player
    }
    double currentQty = getQuantity(); //May be negative in an element
    currentQty += element.getQuantity(); //add to fleet qty
    currentQty = (currentQty) < 0 ? 0 : currentQty; //but prohibit negatives in fleet (resulting from overkill in sim?)
    setQuantity(currentQty);
    return true;
  }

  /**
   * Return the attacks figure that should be used for this elements unit (that
   * is the attacks that were computed when the xfactor was evaluated)
   * @return attacks
   */
  public int getAttacks() {
    return _computedAttacks;
  }

  /**
   * Returns the offense stat to be used for this elements unit (that is the
   * offense that were computed when the xfactor was evaluated)
   * @return offense
   */
  public int getOffense() {
    return _computedOffense;
  }

  /**
   * Returns the defense stat that should be used for this units element (that
   * is the defense that were computed when the xfactor was evaluated)
   * @return defense
   */
  public int getDefense() {
    return _computedDefense;
  }

  /**
   * Returns the PD stat that should be used for this units element. You will
   * need to multiply this by quantity to get the total PD for all units in this
   * element. (This is the PD that was computed when the xfactor was evaluated)
   * @return pd
   */
  public int getPd() {
    return _computedPd;
  }

  /**
   * Returns the unit information for this element.
   * @return unit
   */
  public IUnit getUnit() {
    return _unit;
  }

  /**
   * Set the unit type
   * @param unit
   */
  public void setUnit(IUnit unit) {
    _unit = unit;
  }

  /**
   * Returns the quantity for this element as a double. In some situations (ie:
   * averages lists) this may not be a whole number and may have a fractional
   * part.
   * @return quantity
   */
  public double getQuantity() {
    return _quantity;
  }

  /**
   * Sets the quantity of units in this element. This is a double to facilitate
   * analysis and statistics recording but for combat purposes you would always
   * be working with whole values. Note that setting quantities will cause the
   * causualties count to be reset to zero
   * @param qty
   */
  public void setQuantity(double qty) {
    _quantity = qty;
    _casualties = 0;
  }

  /**
   * Returns the source fleet of the element. These are represented as integers
   * and are used to differentiate elements that originated from different
   * fleets. This is important for the calculation of various x factors.
   */
  public SourceId getSource() {
    return _source;
  }

  /**
   * Set the originating fleet id for the units in this element.
   * This must specify a particular subfleet, you can't pass it "any".
   * @param fleetId
   */
  public void setSource(SourceId source) {
    _source = Objects.requireNonNull(source, "source may not be null");
    if(source.isAny()) {
      throw new IllegalArgumentException("source must specify a specific subfleet here, and not 'any'");
    }
  }

  /**
   * Set the number of casualties marked against this element. Note that
   * casualties in excess of the number of units present is a logic error.
   * @param kills
   * @throws IllegalStateException
   *           if total causualties exceeds quantity
   */
  public void setCasualties(int kills) {
    //20070904AH - Renamed setCasualties since thats what its actually doing!
    _casualties = kills;
    if((double) _casualties > _quantity) {
      throw new IllegalStateException("casualties (" + _casualties + ") > quantity (" + _quantity + ") for " + this);
    }
  }

  /**
   * Returns the current casualty count for this element
   * @return casualties
   */
  public int getCasualties() {
    return _casualties;
  }

  /**
   * Removes casualties from this fleet element and returns the number removed.
   * Note that casualties in excess of the number of units in the element raises
   * an IllegalStateException.
   * @return deadUnitCount
   * @throws IllegalStateException
   *           if total causualties exceeds quantity
   */
  public int clearCasualties() {
    if((double) _casualties > _quantity) {
      throw new IllegalStateException("casualties (" + _casualties + ") > quantity (" + _quantity + ") for " + this);
    }
    _quantity -= (double) _casualties;
    int retval = _casualties;
    _casualties = 0;
    return retval;
  }

  /**
   * Performs hit rolls for the units in the element and returns the number of
   * successfully targetted attack rolls. (These can then be used by the
   * calculator to allocate hits to enemy FleetElements and to make calls to the
   * enemy fleet elements rollDefense once we know how many to fire at each
   * element). Note that the current casualty count is ignored by this method.
   * (Fleets are assumed to fire simultaneously but our logic may have already
   * accumulated kills against this fleet, so we may have some dead men walking
   * who were getting off their shot even as the bullets were hitting them...
   * They will be removed at the end of the combat round)
   * @return hits
   */
  public int rollHits() {
    //System.out.println("roll hits for " + _unit.getAcronym());
    int hits = 0;
    int rolls = getAttacks() * (int) getQuantity();
    int offense = getOffense();
    for(int i = 0; i < rolls; i++) {
      int roll = _rnd.nextInt(100) + 1; //roll 1..100 inclusive
      if(roll <= offense) {
        hits++;
      }
    }
    return hits;
  }

  /**
   * Rolls defence rolls against the incoming hits for units in the element. The
   * number of casuaties from the fire is returned. Note that this method does
   * take into account already killed units. It does this statistically by
   * keeping track of how many causualties are accumulated and doing a roll to
   * see if a hit goes into that bunch or those still living and only tallies
   * deaths for unsuccessful defense rolls of living units. The existing number
   * of casualties agaisnt this element is taken into account, however the
   * method does not change any data so this isnt incremented after the method
   * ends. You would need to do that yourself. Obviously there is some
   * 'retargeting' going on here with the original roll that selected this
   * element not having any further significance as we roll again inside here to
   * work out which unit in the element was really targetted. This does not have
   * any statistical effect on the outcome, but saves us from keeping a kill
   * table. NOTE: this method doesnt make sense for fractional quantities.
   * @param hits
   * @return number of kills from this burst of fire
   */
  public int rollDefence(int hits) {
    if(hits < 0) {
      throw new IllegalArgumentException("hits may not be negative");
    }
    int kills = 0;
    int dead = getCasualties(); //Keep track of proportion of dead units
    int quantity = (int) getQuantity();
    int defense = getDefense();
    //for(int i = 0; i < hits; i++) {
    for(int i = 0; i < hits && (dead < quantity); i++) {
      //First we must work out which unit within the element it was that was
      //targetted. A roll would have been made somewhere else that picked this
      //element based on the index of some unit within the element, but that roll
      //served only to identify the element itself. (There is no point passing
      //it in and reusing it after subtracting the index of first unit in this
      //element. That just creates more work and doesnt change the outcome. Doing
      //a second roll to narrow down the specific unit is much simpler).
      int target = _rnd.nextInt(quantity) + 1; //nb: the first target is numbered 1 (not 0)
      if(target > dead) { //Accumulate deadites at the 'start' of our mental concept of the element
                          //If the roll isnt in that bunch then its one of the live ones so we can
                          //do its defense roll
        int roll = _rnd.nextInt(100) + 1; //roll 1..100 inclusive
        if(roll > defense) { //Fresh victims for the ever growing army of the undead!
          dead++; //Kaboom!
          kills++; //Arrgh!
        } else {
          ; //The hit is deflected by the armour and shielding and the unit
            //will survive to fight on
        }
      } else {
        ; //The shot is wasted! the unit is dead already and only an
          //expanding cloud of debris marks the space it occupied.
          //RIP little buddy
      }
    } //end for hits    
    //We don't store dead here (thats done by a call to setCasualties later)
    //as rollDefense is idempotent
    return kills; //bring out yer dead!
  }

  /**
   * Returns the link back to the parent fleet.
   * @return fleet
   */
  public FleetContents getFleet() {
    return _fleet;
  }

  /**
   * Setter for the parent fleet link (Fleet -- FleetElement is 1:1)
   * @param fleet
   */
  public void setFleet(FleetContents contents) {
    _fleet = contents;
  }

  /**
   * Returns true if the xfactor was found to be active when it was last
   * evaluated
   * @return xfActive
   */
  public boolean isXfActive() {
    return _xfActive;
  }

  /**
   * Mainly used in the fleet table as the contents needs to work out the total
   * FP itself. You need to evaluate the x factors first to get a result.
   * @return fp for this element
   */
  public double getFirepower() {
    double totalAttack = 0;
    double totalSoak = 0;
    IUnit unit = this.getUnit();
    if(unit != null) {
      double qty = this.getQuantity();
      if(unit.isCombatUnit()) {
        totalAttack += qty * (double) this.getOffense() * (double) this.getAttacks();
        totalSoak += qty * (1d / (100d - (double) this.getDefense()));
      }
    }
    double firepower = Math.sqrt(totalAttack * totalSoak);
    return firepower;
  }

  /**
   * Returns the total carry for this fleet element.
   * @return carry total carry for this element
   */
  public double getCarry() {
    return _quantity * (double) _unit.getCarry();
  }

  /**
   * Returns the firepower of an individual unit using the computed attacks,
   * offense, and defense values. X-Factors must therefore be evaluated before
   * this method will return non-zero results.
   * @return ufp Firepower of individual unit taking X into account
   */
  public double getUnitFirepower() {
    IUnit unit = getUnit();
    if(unit == null || !unit.isCombatUnit()) {
      return 0d;
    }
    int attacks = this.getAttacks();
    int offense = this.getOffense();
    int defense = this.getDefense();
    double fp = UnitImpl.calcFirepower(attacks, offense, defense); 
    return fp;
  }

  /**
   * Compares the FleetElement with another FleetElement to see if they are
   * 'equal' for sorting purposes (its based on the sourceId). (If the other
   * object isnt a FleetElement or is null then an exception is thrown)
   * @param o
   *          The other element
   * @return 0 if equal
   * @throws ClassCastException
   *           if o is not a FleetElement
   */
  @Override
  public int compareTo(FleetElement o) {
    if(o==null || !(o instanceof FleetElement)) {
      throw new IllegalArgumentException("o must be a FleetElement (or subclass)");
    }
    FleetElement comparee = (FleetElement) o;

    int srcDif = getSource().compareTo(comparee.getSource()); 
    //nb: when sourceId was an int, the above the previously used: getSource() - comparee.getSource();
    if(srcDif == 0) {
      int unitDif = 0;
      IUnit thisUnit = getUnit();
      IUnit thatUnit = getUnit();
      if(thisUnit == null) {
        unitDif = thatUnit == null ? 0 : -1;
      } else {
        unitDif = thisUnit.compareTo(thatUnit);
      }
      return unitDif;
    } else {
      return srcDif;
    }
  }
  
  @Override
  public String toString() {
    return "FleetElement[" + _unit.getAcronym() + "," + _quantity + "]";
    
  }

}
