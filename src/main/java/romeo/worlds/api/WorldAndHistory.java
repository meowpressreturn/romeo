package romeo.worlds.api;

import java.awt.Color;
import java.util.Objects;

/**
 * Relates a World to an associated History object (for a
 * specific turn). The color, based on owner, is included, as is the scanner
 * range based on the scanner.
 */
public class WorldAndHistory {
  //nb: since 0.6.3 this class replaces IWorldService.WorldHistoryStruct

  public IWorld _world;
  public IHistory _history;
  public Color _color;
  public int _scannerRange;
  public String _team;  
  
  /**
   * Constructor
   * @param world
   * @param history
   * @param color
   * @param scannerRange
   * @param team
   */
  public WorldAndHistory(IWorld world, IHistory history, Color color, int scannerRange, String team) {
    _world = Objects.requireNonNull(world, "world may not be null");
    _history = Objects.requireNonNull(history, "history may not be null");
    _color = Objects.requireNonNull(color, "color may not be null");
    _scannerRange = scannerRange;
    if(scannerRange<0) {
      throw new IllegalArgumentException("scannerRange may not be negative");
    }
    _team = Objects.requireNonNull(team, "team may not be null"); //nb: empty string is fine
  }
  
  @Override
  public String toString() {
    return "WorldAndHistory[" + ((_world==null)? "null" : _world.getName()) + "," 
        + ((_history==null)?"null" : _history.getTurn()) + "]";
  }
  
  public IWorld getWorld() {
    return _world;
  }

  public IHistory getHistory() {
    return _history;
  }
  
  public Color getColor() {
    return _color;
  }
  
  public int getScannerRange() {
    return _scannerRange;
  }
  
  public String getTeam() {
    return _team;
  }
}



















