package romeo.units.impl;

import java.util.Map;

import romeo.importdata.IUnitFile;
import romeo.importdata.IUnitImportReport;
import romeo.importdata.IUnitImporter;
import romeo.importdata.impl.UnitImportReportImpl;

//TODO - dont think this is used. can delete it
public class MockUnitImporter implements IUnitImporter {

  @Override
  public IUnitImportReport importData(IUnitFile unitFile, Map<String, Map<String, String>> adjustmentsMap, boolean update) {
    UnitImportReportImpl report = new UnitImportReportImpl();    
    return report;
  }

}



















