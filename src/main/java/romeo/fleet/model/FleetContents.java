package romeo.fleet.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import romeo.battle.impl.RoundContext;
import romeo.units.api.IUnit;
import romeo.utils.Accumulator;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.NoSuchXFactorException;

/**
 * Class to represent the contents of a fleet. The fleet is modelled as an
 * unordered list of elements that consist of the unit type, the quantity and a
 * number indicating the source fleet. (The elements can also track information
 * pertaining to casualty states during a battle and so forth). It is permitted
 * to have multiple fleet elements of the same unit type and source.
 */
public class FleetContents implements Cloneable, Iterable<FleetElement> {
  protected static Random _rnd = new Random();
  protected List<FleetElement> _elements = new LinkedList<FleetElement>();
  protected Set<String> _flags = new HashSet<String>(); //Set of String

  //.................................................................

  /**
   * No-args constructor to create an empty fleet.
   */
  public FleetContents() {
    ;
  }

  /**
   * Returns true if the specified flag is set, false if not. (There is no
   * difference between not having a flag and it not being set).
   * @param flag
   * @return set
   */
  public boolean hasFlag(String flag) {
    return _flags.contains(flag);
  }

  /**
   * Sets the specified flag. Note that the name will be converted to uppercase.
   * @param flag
   * @param active
   *          true if flag is to be set, false if it is to be removed.
   */
  public void setFlag(String flag, boolean active) {
    Objects.requireNonNull(flag, "flag may not be null");
    if(flag.isEmpty()) {
      throw new IllegalStateException("flag is empty");
    }
    flag = flag.toUpperCase(Locale.US);
    if(active) {
      _flags.add(flag);
    } else {
      _flags.remove(flag);
    }
  }

  /**
   * Returns an iterator over the set of flags. The flags are uppercase strings.
   * @return iterator
   */
  public Iterator<String> getFlags() {
    return _flags.iterator();
  }

  /**
   * Clears all active flags
   */
  public void clearFlags() {
    _flags.clear();
  }

  /**
   * Initialises the contents with clones of the elements in the specified
   * master FleetContents object, and copies all the flags.
   * @param master
   */
  public FleetContents(FleetContents master) {
    for(FleetElement element : master) {
      addElement((FleetElement) element.clone());
    }
    //Copy flags
    Iterator<String> f = master.getFlags();
    while(f.hasNext()) {
      setFlag((String) f.next(), true);
    }
  }

  /**
   * Iterate the FleetElements and invoke execution of their compiled xfactors.
   * This will updated the computed values for unit stats which is what the
   * getAttacks(), getOffense(), etc.. getters in the FleetElement return.
   * WARNING: for this method to work it is necessary that the references to the
   * fleets have already been setup in the {@link RoundContext}
   * @param roundContext
   * @param player
   *          player name of player in round context
   */
  public void evaluateXFactors(RoundContext context) {
    context.setThisPlayer(this); //Set player via fleet (will search context for correct name);
    for(FleetElement element : this) {
      element.evaluateXFactor(context);
    }
  }

  /**
   * Iterate the elements and calculate the total PD value for the fleet.
   * @return PD
   */
  public int getFleetPd() {
    int pd = 0;
    for(FleetElement element : this) {
      pd += element.getPd() * element.getQuantity();
    }
    return pd;
  }

  /**
   * Compile the text xfactors for each element into their object tree
   * representations ready for use. An IXFactorCompiler must be provided to do
   * the actual parsing of the XFEL text.
   * @param compiler
   * @return errorString if the xfactor definition is missing
   */
  public String compileXFactors(IXFactorCompiler compiler) {
    Iterator<FleetElement> i = iterator();
    StringBuffer buffer = new StringBuffer();
    while(i.hasNext()) {
      FleetElement element = (FleetElement) i.next();
      try {
        element.compileXFactor(compiler);
      } catch(NoSuchXFactorException nsx) {
        IUnit unit = element.getUnit();
        buffer.append("Missing X-Factor definition \"");
        buffer.append(unit.getXFactor());
        buffer.append("\" referenced by ");
        buffer.append(unit.getName());
        buffer.append("\n");
      }
    }
    return buffer.toString();
  }

  /**
   * Returns a List of FleetElement whose xFactor is active
   * @return xfElements
   */
  public List<FleetElement> getXFactorElements() {
    ArrayList<FleetElement> results = new ArrayList<FleetElement>(2);
    for(FleetElement element : this) {
      if(element.isXfActive()) {
        results.add(element);
      }
    }
    return results;
  }

  /**
   * Creates a clone of the fleet. Unit instances are shared. FleetElements are
   * individually cloned.
   * @return FleetContents a clone
   */
  @Override
  public Object clone() {
    FleetContents clone = new FleetContents(this);
    return clone;
  }

  /**
   * Calculates and returns the size of the fleet by adding the quantities in
   * all the elements. Note that this method recalculates the size each time
   * rather than caching it, so if you use it in a loop condition expect it to
   * be slow!
   * @param ignoreNonComs
   * @return size Number of individual units represented in the fleet
   */
  public int getSize(boolean ignoreNonComs) {
    int size = 0;
    for(FleetElement element : this) {
      double qty = ignoreNonComs && !element.getUnit().isCombatUnit() ? 0 : element.getQuantity();
      size += qty;
    }
    return size;
  }

  /**
   * Calculates the total firepower of the fleet taking xFactors into account.
   * This method is equivalent to calling getFirepower(false)
   * @return firepower
   */
  public double getFirepower() {
    return getFirepower(false);
  }

  /**
   * Calculates the total firepower of the fleet (which is not quite the same as
   * summing the individual element fp). Casualty counts are ignored, it is
   * assumed casualties have been removed.
   * @param raw
   *          specify true to ignore xFactors and return the raw firepower
   * @return firepower
   */
  public double getFirepower(boolean raw) {
    double totalAttack = 0;
    double totalSoak = 0;
    for(FleetElement element : this) {
      double qty = element.getQuantity();
      if(element.getUnit().isCombatUnit()) {
        int offense = raw ? element.getUnit().getOffense() : element.getOffense();
        int attacks = raw ? element.getUnit().getAttacks() : element.getAttacks();
        int defense = raw ? element.getUnit().getDefense() : element.getDefense();
        totalAttack += (double) (offense * attacks) * qty;
        totalSoak += qty * (1d / (100d - (double) defense));
      }
    }
    double firepower = Math.sqrt(totalAttack * totalSoak);
    return firepower;
  }

  /**
   * Returns the total carry of the fleet. Casualty counts are ignored, it is
   * assumed casualties have been removed.
   * @return carry
   */
  public double getCarry() {
    double carry = 0;
    for(FleetElement element : this) {
      carry += element.getCarry();
    }
    return carry;
  }

  /**
   * Returns an iterator over the elements in the fleet
   * @return iterator
   */
  @Override
  public ListIterator<FleetElement> iterator() {
    return _elements.listIterator();
  }

  /**
   * Returns the element that contains the individual unit at the specified
   * index. This method is primarily used during the targetting phase of combat.
   * Warning: unit quantities in an element allow fractions. This doesnt make
   * sense in combat but is useful when performing other forms of analyses. This
   * method will not deliver expected results with fleets containing fractional
   * elements! If ignoreNoncoms is true then this method will skip over elements
   * for non-combat units (which is what we want in battles)
   * @param index
   *          An index from 0..size-1
   * @param ignoreNoncoms
   * @return the FleetElement containing the specified individual unit instance
   */
  public FleetElement getElementContaining(int index, boolean ignoreNoncoms) {
    int counter = 0;
    for(FleetElement element : this) {
      if(element.getUnit().isCombatUnit() || !ignoreNoncoms) { //If its a combat unit or we arent ignoring noncoms then check if
                                                                 //index falls into this element
        int qty = (int) element.getQuantity(); //this cast will trash fractions
        int offsetIndex = index - counter;
        if(offsetIndex < qty) {
          return element;
        }
        counter += qty;
      }
    }
    throw new IndexOutOfBoundsException("Out of range unit index:" + index);
  }

  /**
   * Adds an element to the fleet. If there is a suitable existing slot then the
   * contents will be added to it. If not the element will be used to create a
   * new slot. The elements fleet reference will also be set.
   * @param element
   */
  public void addElement(FleetElement element) {
    if(element == null) {
      throw new NullPointerException("null element");
    }
    for(FleetElement slot : this) {
      if(slot.getClass() == element.getClass() && slot.getUnit() == element.getUnit()) { //Must be same type of class, unit and source to reuse element
        if(slot.add(element)) {
          return; //Short circuit return - important or we would add twice!
        }
      }
    }
    //If we reach here a suitable slot was not found so we shall create one
    element.setFleet(this);
    _elements.add(element);
  }

  /**
   * Remove a FleetElement. This takes a {@link FleetElement} that is used as a
   * profile to identify the actual fleet element to be removed. The first one
   * matching its unit and source will be removed. Nb: Must be same
   * class/subclass of FleetElement. The profile object is considered throwaway
   * and will be modified by the method.
   * @param profile
   *          A Profile of the element to remove. This WILL be modified. TODO -
   *          unit test for this. I dont think it works quite as described. Is
   *          the description wrong or the code?
   */
  public void removeElement(FleetElement profile) {
    if(profile == null)
      throw new NullPointerException("null profile");
    for(FleetElement slot : this) {
      if(slot.getClass() == profile.getClass() && slot.getUnit() == profile.getUnit()) { //Must be same type of class, unit and source to reuse element
        profile.setQuantity(slot.getQuantity() * -1d);
        if(slot.add(profile)) {
          return; //Short circuit return - important or we would add twice!
        }
      }
    }
    //If we reach here a suitable slot was not found 
    //(So no need to do anything)
  }

  /**
   * Sorts the contents of the fleet based on source and unit
   */
  public void sort() {
    Collections.sort(_elements); //FleetElement impls Comparator<FleetElement>
  }

  /**
   * Removes all noncombat elements from this fleet and adds them to the target
   * fleet. If the target is this fleet then this method does nothing.
   * @param target
   *          (may be null)
   */
  public void transferNonCombatUnitsTo(FleetContents target) {
    if(target != this) {
      ListIterator<FleetElement> i = iterator();
      while(i.hasNext()) {
        FleetElement element = (FleetElement) i.next();
        if(!element.getUnit().isCombatUnit()) {
          i.remove();
          if(target != null) {
            target.addElement(element);
          }
        }
      }
    }
  }

  /**
   * Rolls hits for the fleet. Casualty units also get to fire as firing of all
   * fleets in a battle is simultaneous yet we only calculate it one at a time.
   * (Well actually we can and might calc it in advance because still got the
   * def rolls to go but doing it like this adds some flexibility to impls.)
   * @return hits Number of successful attack rolls
   */
  public int rollHits() {
    int hits = 0;
    for(FleetElement element : this) {
      hits += element.rollHits();
    }
    return hits;
  }

  /**
   * Randomly assigns the hits to units in the fleet and then rolls defense
   * rolls against them. Casualties are then recorded in the appropriate
   * elements. The total number of new kills added across the elements is
   * returned. Elements may have existing casualties recorded. These are taken
   * into account.
   * @param hits
   *          Number of incoming hits to fire on fleet
   * @return Number of kills as a result of the barrage
   */
  public int fireOnFleet(int hits) {
    int kills = 0;
    //We first allocate our hits across the elements in the fleet
    //This involves rolling hitsDsize to choose the index of a unit
    //in an element. This is used to determine the targetted element
    //and we register that hit against the element in an accumulator map
    //(The FleetElement is used as the key)
    int size = getSize(true);
    Accumulator hitsPerElement = new Accumulator();
    synchronized(_rnd) //_rnd is shared between instances so sync in case of threading TODO: wtf? why even thread then? give each its own!
    {
      for(int i = 0; i < hits; i++) { //For each incomming hit select an index in the conceptual
                                        //array of individual units in the fleet.
        int unitIndex = _rnd.nextInt(size);
        //By doing rnd(size) rather than rnd(unitCount) we assure that we
        //are firing against elements in proportion to how many units are in them.
        //Now we determine which element we targetted based on the rolled index
        FleetElement targetElement = getElementContaining(unitIndex, true);
        //And register a hit against that element
        hitsPerElement.addToValue(targetElement, 1);
        //You will note that we discard the rolled index and so we still dont
        //know which particular individual unit in the element was the target.
        //That will come later.       
      }
    }
    //Now that we have allocated each of the incoming hits against a
    //particular unit we can go and do the defense rolls and start
    //marking casualties. First we get a list of elements that ended up
    //getting one or more hits accumulated against them. (Technically
    //we could also our full elements list and still get the correct
    //result).
    Object[] hitElements = hitsPerElement.getKeys();
    for(int i = 0; i < hitElements.length; i++) { //For each element that has hits registered against it
      FleetElement hitElement = (FleetElement) hitElements[i];
      //we take those hits
      int hitsForElement = hitsPerElement.getValue(hitElement);
      //and do defense rolls against them to get the number of hits
      //that result in a kill
      int killedByFire = hitElement.rollDefence(hitsForElement);
      //which we then record against the element
      int alreadyDead = hitElement.getCasualties();
      //Round 2 biobombs for example
      hitElement.setCasualties(alreadyDead + killedByFire);
      //and add to our total number of kills
      kills += killedByFire;
    }
    return kills;
  }

  /**
   * Removes casualties from elements that have casualties recorded against
   * them. Note that it is considered a logic error to have more casualties than
   * units to remove. The total number of casualties removed is returned.
   * @return deadUnitCount
   */
  public int clearCasualties() {
    int totalRemoved = 0;
    for(FleetElement element : this) {
      totalRemoved += element.clearCasualties();
    }
    return totalRemoved;
  }

  /**
   * Adds the contents of the specified FleetContents to this one. (This is done
   * by simply creating a new {@link FleetElement} in this fleet with the copied
   * quantity). The 'flat' flag indicates that all units are to be assigned a
   * sourceId or 0 instead of retaining their sourceId in the source
   * FleetContents.
   * @param source
   * @param flat
   */
  public void addFleet(FleetContents source, boolean flat) {
    for(FleetElement srcElement : source.getElements()) {
      double quantity = srcElement.getQuantity();
      int sourceId = flat ? 0 : srcElement.getSource();
      FleetElement element = new FleetElement(srcElement.getUnit(), quantity, sourceId);
      this.getElements().add(element);
    }
  }

  /**
   * Will flatten multiple {@link FleetElement} having the same unit and source
   * into a single FleetElement. Does not sort. If the flat flag is set to true
   * then it will flatten all elements of a type into sourceId zero. This loses
   * source information and xfactors. Flattened fleets are used in reporting,
   * they should not be used in simulation.
   * @param flat
   */
  public void normalise(boolean flat) {
    Map<String, FleetElement> index = new HashMap<String, FleetElement>();
    for(FleetElement element : this) {
      IUnit unit = element.getUnit();
      double qty = element.getQuantity();
      if(qty > 0) {
        int sourceId = flat ? 0 : element.getSource();
        String key = flat ? unit.getName() : unit.getName() + "::" + sourceId;
        FleetElement normalisedElement = (FleetElement) index.get(key);
        if(normalisedElement == null) { //If we havent got a single element to use for this source and unit yet then
                                          //we obtain one either by reusing the first we find (to preserve xfactors) or
                                        //if we are flattening we create a new one (discarding any xfactor in the process)
          normalisedElement = flat ? new FleetElement(unit, qty, sourceId) : element;
          index.put(key, normalisedElement);
        } else {
          normalisedElement.setQuantity(normalisedElement.getQuantity() + qty);
        }
      }
    }
    _elements.clear();
    _elements.addAll(index.values());
  }

  /**
   * Divides all the quantities in the fleet by the specified value.
   * @param value
   * @return newTotal
   */
  public double divideQuantity(double by) {
    double total = 0;
    for(FleetElement element : this) {
      double qty = element.getQuantity();
      qty /= by;
      element.setQuantity(qty);
      total += qty;
    }
    return total;
  }

  /**
   * Returns the list of FleetElement instances that comprise the fleet. Note
   * that this returns the internal list used to maintain the elements. (To add
   * an element add it to this list). If you call methods that modify this list
   * such as addFleet() while also trying to iterate it you may get a
   * {@link ConcurrentModificationException}. The list returned may be empty, it
   * will never be null.
   * @return elements
   */
  public List<FleetElement> getElements() {
    return _elements;
  }
}
