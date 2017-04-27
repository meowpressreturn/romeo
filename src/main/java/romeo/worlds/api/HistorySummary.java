package romeo.worlds.api;

import java.util.Objects;

/**
 * Holds summary information based on world history for a given owner across multiple turns.
 */
public class HistorySummary {
  
  //This class replaces IWorldService.HistorySummaryStruct
  
  public String _owner; //this is owner name and NOT id
  public double _totalFirepower;
  public int _totalLabour;
  public int _totalCapital;
  public int _worldCount;
  
  /**
   * Constructor that returns a summary with an empty owner name and zeros for the totals.
   */
  public HistorySummary() {
    this("",0,0,0,0);
  }
  
  /**
   * Constructor
   * @param owner name of a player, may not be null but empty is permitted
   * @param totalFirepower
   * @param totalLabour
   * @param totalCapital
   */
  public HistorySummary(String owner, double totalFirepower, int totalLabour, int totalCapital, int worldCount) {
    _owner = Objects.requireNonNull(owner, "owner may not be null");
    _totalFirepower = totalFirepower;
    _totalLabour = totalLabour;
    _totalCapital = totalCapital; 
    _worldCount = worldCount;
  }
  
  /**
   * Returns the owner. This is a name and not an id.
   * It is not (currently) required for it to match a name in players, but many Romeo features are based
   * on the assumption that it will.
   * @return owner
   */
  public String getOwner() {
    return _owner;
  }
  
  public double getTotalFirepower() {
    return _totalFirepower;
  }
  
  public int getTotalLabour() {
    return _totalLabour;
  }
  
  public int getTotalCapital() {
    return _totalCapital;
  }
  
  public int getWorldCount() {
    return _worldCount;
  }
  
  
}



















