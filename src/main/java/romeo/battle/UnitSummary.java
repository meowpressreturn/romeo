//2008-12-01
package romeo.battle;

import romeo.units.api.IUnit;

/**
 * Simple bean that holds information on the average survival numbers of a
 * particular type of unit.
 */
public class UnitSummary {
  private IUnit _unit;
  private int _initialQuantity;
  private double _averageSurvivors;
  private double _adjustedAverageSurvivors;

  public UnitSummary() {
    ;
  }

  public IUnit getUnit() {
    return _unit;
  }

  public void setUnit(IUnit unit) {
    _unit = unit;
  }

  public int getInitialQuantity() {
    return _initialQuantity;
  }

  public void setInitialQuantity(int initialQuantity) {
    _initialQuantity = initialQuantity;
  }

  public double getAverageSurvivors() {
    return _averageSurvivors;
  }

  public void setAverageSurvivors(double averageSurvivors) {
    _averageSurvivors = averageSurvivors;
  }

  public double getAdjustedAverageSurvivors() {
    return _adjustedAverageSurvivors;
  }

  public void setAdjustedAverageSurvivors(double adjustedAverageSurvivors) {
    _adjustedAverageSurvivors = adjustedAverageSurvivors;
  }

}
