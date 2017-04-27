package romeo.xfactors.impl;

import java.util.Objects;

import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.XFactorId;

/**
 * Immutable bean that holds the id, name, description, and expressions used to model an xfactor.
 * This class is now immutable. To modify you need to create a new instance.
 */
public class XFactorImpl implements IXFactor {
  private XFactorId _id;
  private String _name;
  private String _description;
  private String _trigger;
  private String _xfAttacks;
  private String _xfOffense;
  private String _xfDefense;
  private String _xfPd;
  private String _xfRemove;

  /**
   * Creates an x-factor record with a null id and empty values for its fields.
   */
  public XFactorImpl() {
    this(null,"","","","","","","","");
  }
  
  /**
   * Constructor. The id may be null in order to specify this is a new record.
   * All other values may not be null (but may be empty strings)
   * @param id
   *          db pk or null if new
   * @param name
   *          name of the xfactor. Must be specified.
   * @param description
   * @param trigger
   *          xfel expression evaluating when the xfactor is triggered
   * @param xfAttacks
   *          xfel expression returning the altered number of attacks
   * @param xfOffense
   *          xfel expression returning the altered offense
   * @param xfDefense
   *          xfel expression returning the altered defense
   * @param xfPd
   *          xfel expression returning the altered population damage
   * @param xfRemove
   *          xfel expression indicating when the unit gets preemptively removed
   *          from battle
   */
  public XFactorImpl(
      XFactorId id,
      String name,
      String description,
      String trigger,
      String xfAttacks,
      String xfOffense,
      String xfDefense,
      String xfPd,
      String xfRemove) {
    _id = id; //May be null
    _name = Objects.requireNonNull(name, "name may not be null").trim();
    _description = Objects.requireNonNull(description, "description may not be null");
    _trigger = Objects.requireNonNull(trigger, "trigger may not be null");
    _xfAttacks = Objects.requireNonNull(xfAttacks, "afAttacks may not be null");
    _xfOffense = Objects.requireNonNull(xfOffense, "xfOffense may not be null");
    _xfDefense = Objects.requireNonNull(xfDefense, "xfDefense may not be null");
    _xfPd = Objects.requireNonNull(xfPd, "xfPd may not be null");
    _xfRemove = Objects.requireNonNull(xfRemove, "xfRemove may not be null");    
  }

  /**
   * Creates a new XFactor impl using the values from the source provided, but
   * with the id specified (ignores the id in the source). Note that the source
   * may not provide nulls for any of the values.
   * @param id
   *          db PK id or null if this is a new record
   * @param source
   *          xFactor from which values are copied (may not be null)
   */
  public XFactorImpl(XFactorId id, IXFactor source) {
    this( Objects.requireNonNull(source, "source may not be null").getId(),
          source.getName(),
          source.getDescription(),
          source.getTrigger(),
          source.getXfAttacks(),
          source.getXfOffense(),
          source.getXfDefense(),
          source.getXfPd(),
          source.getXfRemove() );   
  }
  
  /**
   * Returns the primary key in the db of this record. If this is a new record this will be null
   * @return id 
   */
  @Override
  public XFactorId getId() {
    return _id;
  }
  
  /**
   * Returns true if the id is null (indicating that this record wasn't persisted)
   * @return
   */
  @Override
  public boolean isNew() {
    return _id == null;
  }

  @Override
  public String toString() {
    return _name.isEmpty() ? "Unnamed " + _id : _name;
  }

  @Override
  public String getDescription() {
    return _description;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getTrigger() {
    return _trigger;
  }

  @Override
  public String getXfAttacks() {
    return _xfAttacks;
  }

  @Override
  public String getXfDefense() {
    return _xfDefense;
  }

  @Override
  public String getXfOffense() {
    return _xfOffense;
  }

  @Override
  public String getXfPd() {
    return _xfPd;
  }

  @Override
  public String getXfRemove() {
    return _xfRemove;
  }

}