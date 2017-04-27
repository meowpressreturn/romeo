package romeo.importdata.impl;

import romeo.importdata.IWorldImportReport;

public class WorldImportReportImpl implements IWorldImportReport {
  
  //as of 0.6.3 we no longer store the actual records here
  private int _importedWorlds;
  private int _updatedWorlds;
  private int _importedPlayers;
  
  private Exception _exception;

  /**
   * Constructor
   */
  public WorldImportReportImpl() {
    ;
  }

  /**
   * Increment the count of worlds updated
   */
  public void addUpdatedWorld() {
    _updatedWorlds++;
  }

  /**
   * Increment the count of worlds imported
   */
  public void addImportedWorld() {
    _importedWorlds++;
  }

  /**
   * Increment the count of players imported
   */
  public void addImportedPlayerX() {
    _importedPlayers++;
  }
  
  /**
   * Set the exception reference. This indicates that the import had an error.
   * @param e
   */
  public void setException(Exception e){
    _exception = e;
  }

  /**
   * Return any exception that was thrown while perfoming the report and stored
   * here
   * @return exception
   */
  @Override
  public Exception getException() {
    return _exception;
  }

  @Override
  public int getImportedWorldsCount() {
    return _importedWorlds;
  }

  @Override
  public int getUpdatedWorldsCount() {
    return _updatedWorlds;
  }

  @Override
  public int getImportedPlayersCount() {
    return _importedPlayers;
  }

  
}
