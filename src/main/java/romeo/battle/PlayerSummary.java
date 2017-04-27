package romeo.battle;

import java.util.List;

/**
 * Bean that holds summary info about a single player. These are intended to
 * support the convenient reporting of results, such as via a BeanTableModel
 * etc...
 */
public class PlayerSummary {
  private String _name;
  private int _winCount;
  private double _winPercent;
  private double _averageSurvivingFirepower;
  private double _adjustedAverageSurvivingFirepower;
  private List<UnitSummary> _survivors;

  /**
   * No-args constructor
   */
  public PlayerSummary() {
    ;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public int getWinCount() {
    return _winCount;
  }

  public void setWinCount(int winCount) {
    _winCount = winCount;
  }

  public double getAverageSurvivingFirepower() {
    return _averageSurvivingFirepower;
  }

  public void setAverageSurvivingFirepower(double averageSurvivingFirepower) {
    _averageSurvivingFirepower = averageSurvivingFirepower;
  }

  public double getAdjustedAverageSurvivingFirepower() {
    return _adjustedAverageSurvivingFirepower;
  }

  public void setAdjustedAverageSurvivingFirepower(double adjustedAverageSurvivingFirepower) {
    _adjustedAverageSurvivingFirepower = adjustedAverageSurvivingFirepower;
  }

  /**
   * Returns the win percentage. Note that this is in the 0..100 range (unlike
   * in IBattleMetrics itself which returns a fraction)
   * @return winPercent 0..100
   */
  public double getWinPercent() {
    return _winPercent;
  }

  public void setWinPercent(double winPercent) {
    _winPercent = winPercent;
  }

  public List<UnitSummary> getSurvivors() {
    return _survivors;
  }

  public void setSurvivors(List<UnitSummary> survivors) {
    _survivors = survivors;
  }

}
