package romeo.units.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import romeo.persistence.IdBean;
import romeo.units.api.IUnit;
import romeo.units.api.UnitId;
import romeo.xfactors.api.XFactorId;

/**
 * A class that may be used to hold information about a unit. 
 * As of 0.6.3 it is now immutable. 
 * equals and hashCode are not overridden (so only work on reference equality)
 * however compareTo has been implemented for sorting convenience and is based on
 * name.
 * Static utility methods have been provided that will copy unit data into and out of a Map.
 */
public class UnitImpl extends IdBean<UnitId> implements IUnit {
  
  /**
   * Static utility method for calculating firepower. Will return zero if any of
   * the parameters is 0)
   * @param attacks
   * @param offense
   * @param defense
   * @return fp
   */
  public static double calcFirepower(int attacks, int offense, int defense) {
    if(attacks < 1 || offense < 1 || defense < 1) {
      return 0;
    }
    double offensive = attacks * offense;
    double defensive = 1d / (double) (100 - defense);
    return Math.sqrt(offensive * defensive);
  }
  
  public static UnitImpl createFromMap(Map<String, Object> data) {
    //TODO - use sensible defaults for missing values (currently will throw NPE)
    UnitId id = (UnitId)data.get("id");
    String name = (String)data.get("name");
    Integer attacks = (Integer)data.get("attacks");
    Integer offense = (Integer)data.get("offense");
    Integer defense = (Integer)data.get("defense");
    Integer pd = (Integer)data.get("pd");
    Integer speed = (Integer)data.get("speed");
    Integer carry = (Integer)data.get("carry");
    Integer cost = (Integer)data.get("cost");
    Integer complexity = (Integer)data.get("complexity");
    Integer scanner = (Integer)data.get("scanner");
    Integer license = (Integer)data.get("license");
    String acronym = (String)data.get("acronym");
    XFactorId xFactor = (XFactorId)data.get("xFactor");
    UnitImpl unit = new UnitImpl(id, name, attacks, offense, defense, pd, speed, carry, cost, complexity, scanner,
        license, acronym, xFactor);
    return unit;
  }
  
  /**
   * Creates and returns a map containing the properties of the unit. The map returned is not backed by the
   * unit and map be freely mutated. Maintains the typing of the unit properties, albeit using wrappers for
   * the primitives.
   * @param unit
   * @return new map
   */
  public static Map<String,Object> asMap(IUnit unit) {
    Objects.requireNonNull(unit, "unit may not be null");
    Map<String,Object> map = new HashMap<>();
    map.put("id",unit.getId());
    map.put("name",unit.getName());
    map.put("attacks",unit.getAttacks());
    map.put("offense",unit.getOffense());
    map.put("defense",unit.getDefense());
    map.put("pd",unit.getPd());
    map.put("speed",unit.getSpeed());
    map.put("carry",unit.getCarry());
    map.put("cost",unit.getCost());
    map.put("complexity",unit.getComplexity());
    map.put("scanner",unit.getScanner());
    map.put("license",unit.getLicense());
    map.put("acronym",unit.getAcronym());
    map.put("xFactor",unit.getXFactor()); 
    return map;
  }
  
  public static String generatePlaceholderAcronym(String name) {
    if(name==null || name.trim().isEmpty()) {
      return "UNTITLED";
    } else {
      //String acronym = name.trim().toUpperCase(Locale.US);
      StringBuilder b = new StringBuilder();
      int l = name.length();
      for(int i=0; i<l; i++) {
        char  c = name.charAt(i);
        if(Character.isAlphabetic(c) || Character.isDigit(c)) {
          b.append(c);
        }
      }
      String acronym = b.toString().toUpperCase(Locale.US);
      return acronym;
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  
  private String _name;
  private int _attacks;
  private int _offense;
  private int _defense;
  private int _pd;
  private int _speed;
  private int _carry;
  private int _cost;
  private int _complexity;
  private int _scanner;
  private int _license;
  private String _acronym;
  private XFactorId _xfactor;
  
  /**
   * Copy constructor. Allows you to make an identical copy differing only in id.
   * @param id
   * @param source
   */
  public UnitImpl(UnitId id, IUnit source) {
    this(id, source.getName(), source.getAttacks(), source.getOffense(), source.getDefense(), source.getPd(),
        source.getSpeed(), source.getCarry(), source.getCost(), source.getComplexity(), source.getScanner(),
        source.getLicense(), source.getAcronym(), source.getXFactor());
  }
  
  /**
   * Constructor
   * @param id if the unit is new this should be null
   * @param name
   * @param attacks
   * @param offense
   * @param defense
   * @param pd
   * @param speed
   * @param carry
   * @param cost
   * @param complexity
   * @param scanner
   * @param license
   * @param acronym
   * @param xFactor id of this units x-factor or null if no x-factor applied to this unit
   */
  public UnitImpl(UnitId id,
                  String name,
                  int attacks,
                  int offense,
                  int defense,
                  int pd,
                  int speed,
                  int carry,
                  int cost,
                  int complexity,
                  int scanner,
                  int license,
                  String acronym,
                  XFactorId xFactor) {
    setId(id);
    _name = Objects.requireNonNull(name, "name may not be null").trim();
    if(attacks < 0) { throw new IllegalArgumentException("attacks may not be negative"); }
    if(offense < 0) { throw new IllegalArgumentException("offense may not be negative"); }
    if(defense < 0) { throw new IllegalArgumentException("defense may not be negative"); }
    if(pd < 0) { throw new IllegalArgumentException("pd may not be negative"); }
    if(speed < 0) { throw new IllegalArgumentException("speed may not be negative"); }
    if(cost < 0) { throw new IllegalArgumentException("cost may not be negative"); }
    if(complexity < 0) { throw new IllegalArgumentException("complexity may not be negative"); }
    if(scanner < 0) { throw new IllegalArgumentException("scanner may not be negative"); }
    if(license < 0) { throw new IllegalArgumentException("license may not be negative"); }
    _attacks = attacks;
    _offense = offense;
    _defense = defense;
    _pd = pd;
    _speed = speed;
    _carry = carry;
    _cost = cost;
    _complexity = complexity;
    _scanner = scanner;
    _license = license;
    _acronym = Objects.requireNonNull(acronym, "acronym may not be null").trim();
    _xfactor = xFactor;
  }

  @Override
  public String toString() {
    return "UnitImpl[id=" + getId() + ", name=" + getName() + "]";
  }

  /**
   * Returns true if this is a combat unit (ie: if it has non-zero offense and
   * attacks or defense) (Pd is currently not considered)
   * @return combatUnit
   */
  @Override
  public boolean isCombatUnit() {
    return (_offense > 0 && _attacks > 0) || _defense > 0; //what about pd?
  }

  /**
   * Returns a units firepower based on its attacks, offense, and defense
   * @return firepower
   */
  @Override
  public double getFirepower() {
    return calcFirepower(_attacks, _offense, _defense);
  }

  @Override
  public int getAttacks() {
    return _attacks;
  }

  @Override
  public int getCarry() {
    return _carry;
  }

  @Override
  public int getComplexity() {
    return _complexity;
  }

  @Override
  public int getCost() {
    return _cost;
  }

  @Override
  public int getDefense() {
    return _defense;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int getOffense() {
    return _offense;
  }

  @Override
  public int getPd() {
    return _pd;
  }

  @Override
  public int getScanner() {
    return _scanner;
  }

  @Override
  public int getSpeed() {
    return _speed;
  }

  @Override
  public int getLicense() {
    return _license;
  }

  /**
   * Returns the units acronymn. If this is null an empty string is returned.
   * Never null.
   * @return acronym
   */
  @Override
  public String getAcronym() {
    return _acronym == null ? "" : _acronym;
  }

  /**
   * Compares units to other units based on their name
   * @param o
   * @return equals 0 if not equals
   */
  @Override
  public int compareTo(Object o) {
    if(o instanceof IUnit) {
      String thisName = getName();
      thisName = thisName == null ? "" : thisName;
      IUnit unit = (IUnit) o;
      String thatName = unit.getName();
      thatName = thatName == null ? "" : thatName;
      return thisName.compareTo(thatName);
    }
    return 0;
  }

  /**
   * Returns the ID of the xFactor attached to this unit. This will return null
   * for those units that don't have an xfactor.
   * @return xFactor 
   */
  @Override
  public XFactorId getXFactor() {
    return _xfactor;
  }


}