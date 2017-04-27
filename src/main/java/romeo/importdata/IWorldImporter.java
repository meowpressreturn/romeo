package romeo.importdata;

/**
 * Interface for object used to import world, historyu and player data from a
 * map csv. Import settings are configured on the implementation instance.
 */
public interface IWorldImporter {
  /**
   * Import world and player data from the csv file. Any exceptions will be
   * returned in the report object.
   * @param worldFile
   *          interface to object representing the map csv
   * @param turn
   *          turn against which to record historical values
   * @return report
   */
  public IWorldImportReport importData(IWorldFile worldFile, int turn);
}
