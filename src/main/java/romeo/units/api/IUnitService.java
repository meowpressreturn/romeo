package romeo.units.api;

import java.util.List;

import romeo.model.api.IService;
import romeo.persistence.IExternalDataChangeListener;
import romeo.xfactors.api.XFactorId;

/**
 * Public interface to the unit service that is used to manage the persisted
 * data about the various units. The calls that modify unit data or create new
 * units in the database will also cause any listeners against this service to
 * be notified.
 */
public interface IUnitService extends IService, IExternalDataChangeListener {

  /**
   * Get a list of all the units
   * @return list of Unit
   */
  public List<IUnit> getUnits();

  /**
   * Write the data in the unit to the database, creating new rows or updating
   * existing rows as necessary. Notifies any listeners to this service. The
   * romeo id allocated to the saved unit is returned.
   * @param unit
   * @return id
   */
  public UnitId saveUnit(IUnit unit);

  /**
   * Write the data in all the units in the list to the database. Notifies any
   * listeners to this service.
   * @param units
   *          a List of Unit
   * @return unitIds
   */
  public List<UnitId> saveUnits(List<IUnit> units);

  /**
   * Return a single unit's information given the unit id
   * @param id
   */
  public IUnit getUnit(UnitId id);

  /**
   * Return a single unit's information given its acronymn
   * @param acronym
   */
  public IUnit getByAcronym(String acronym);

  /**
   * Return a single unit given its name
   * @param name
   */
  public IUnit getByName(String name);

  /**
   * Remove the row for a unit given its id. Notifies any listeners to this
   * service.
   * @param id
   */
  public void deleteUnit(UnitId id);

  /**
   * Returns an array containing a unique and ordered list of the various speeds
   * available among all the units. (Does not include 0)
   * @return speeds
   */
  public int[] getSpeeds();

  /**
   * Returns the min and max values of a property taken across all the units in
   * the database. Will cache these values after the first demand. This method
   * also supports the use of "multipliedOffense" and "logisticsFactor" for the
   * property, although these are not longer an actual property exposed via the
   * IUnit interface.
   * @param property
   * @return range array [min,max]
   */
  public double[] getRange(String property);

  /**
   * Returns all the units that have a scanner range range greater than zero in
   * ascending order of scanner range. (Note this list doesnt contain the
   * default no-scanner scanner range).
   * @return scanners
   */
  public List<IUnit> getScanners();
  
  /**
   * Clears the xFactor reference of any units that link the specified xFactor
   * @param id the X-Factor to unlink from units
   */
  public void unlinkUnitsWithXFactor(XFactorId id);

}
