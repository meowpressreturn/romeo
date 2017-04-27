package romeo.importdata.impl;

import romeo.importdata.IUnitImportReport;

/**
 * Implementation of IImportReport. This is mutable and provides adder methods that can be called during the import.
 */
public class UnitImportReportImpl implements IUnitImportReport {

  //nb: before 0.6.3 these were lists of actual units
  private int _imported = 0; 
  private int _updated = 0;
  
  private Exception _exception;

  /**
   * Constructor
   */
  public UnitImportReportImpl() {
    ;
  }

  /**
   * Add a unit to the count of units that were updated
   * @param unit
   */
  public void addUpdated() {
    _updated++;
  }

  /**
   * Add a unit to the count of units that were freshly imported
   * @param unit
   */
  public void addImported() {
    _imported++;
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

  /**
   * Store an exception to be reported (only one may be stored)
   * @param exception
   */
  public void setException(Exception exception) {
    _exception = exception;
  }

  @Override
  public int getImportedUnitsCount() {
    return _imported;
  }

  @Override
  public int getUpdatedUnitsCount() {
    return _updated;
  }

}
