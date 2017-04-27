package romeo.importdata;

/**
 * Interface for an object that provides some simple feedback on the outcome of
 * a unit import operation
 */
public interface IUnitImportReport {

  /**
   * Returns the number of units that were created as a result of the import operation
   * @return
   */
  public int getImportedUnitsCount();

  /**
   * Returns the number of existing units that were updated as a result of the import operation
   * @return
   */
  public int getUpdatedUnitsCount();

  /**
   * Returns the exception if something went wrong. A value for this property
   * indicates an error in the import.
   * @return exception
   */
  public Exception getException();
}
