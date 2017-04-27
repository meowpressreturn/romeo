package romeo.importdata;

import java.util.Map;

import romeo.importdata.impl.UnitImporterImpl;

/**
 * Interface for invoking the import of unit data from a unit file.
 * Implementation is in {@link UnitImporterImpl}
 */
public interface IUnitImporter {
  /**
   * Invokes the import of unit data into Romeo from a parsed unit csv file
   * @param unitFile
   * @param adjustments optional map of adjustments to be made to the imported data
   * @param enableUpdate set to true to allow existing units to be updated
   */
  public IUnitImportReport importData(IUnitFile unitFile, Map<String, Map<String, String>> adjustments, boolean enableUpdate);
}
