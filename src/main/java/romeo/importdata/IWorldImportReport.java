package romeo.importdata;

/**
 * Interface for the report from the world importer
 */
public interface IWorldImportReport {
  
  /**
   * Returns the number of world records that were created as a result of the import
   * @return imported
   */
  public int getImportedWorldsCount(); 
  
  /**
   * Returns the number of worlds that were modified as a result of the import operation
   * @return updated
   */
  public int getUpdatedWorldsCount();

  /**
   * Returns the number of player records created as a result of the world import operation
   * @return
   */
  public int getImportedPlayersCount();

  /**
   * Returns the exception if something went wrong. A value for this property
   * indicates an error in the import.
   * @return exception
   */
  public Exception getException();
}
