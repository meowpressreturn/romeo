package romeo.battle.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import romeo.utils.MutableDouble;

/**
 * Wraps a map of mutable doubles indexed by player name
 */
public class PlayerIndexedValues {
  private Map<String, MutableDouble> _index = new HashMap<String, MutableDouble>();
  private String[] _players;

  /**
   * Constructor. Requires the player names to build the index.
   * @param players
   */
  public PlayerIndexedValues(String[] players) {
    _players = Objects.requireNonNull(players, "players may not be null");
    if(players.length < 1) {
      throw new IllegalArgumentException("No players specified");
    }    
    for(int p = players.length - 1; p >= 0; p--) {
      String player = players[p];
      if(player == null || player.length() == 0) {
        throw new IllegalArgumentException("Player name not specified at index " + p);
      }
      if(_index.containsKey(player)) {
        throw new IllegalArgumentException("Player already specified:" + player);
      }
      _index.put(player, new MutableDouble(0));
    }
  }

  /**
   * Resets all the counters to zero
   */
  public void clear() {
    for(int p = _players.length - 1; p >= 0; p--) { //We reset the value in the holder rather than recreating holder as someone may have ref to it
      MutableDouble holder = (MutableDouble) _index.get(_players[p]);
      holder.setValue(0);
    }
  }

  /**
   * Returns the list of player names for which there are values
   * @return players
   */
  public String[] getPlayers() {
    return _players;
  }

  /**
   * Returns the value for the specified player
   * @param player
   * @return value
   */
  public double getValue(String player) {
    return getHolder(player).doubleValue();
  }

  /**
   * Sets the value for the specified player
   */
  public void setValue(String player, double value) {
    getHolder(player).setValue(value);
  }

  /**
   * Increase the value for the specified player by delta
   * @param player
   * @param delta
   */
  public void addValue(String player, double delta) {
    getHolder(player).addValue(delta);
  }

  /**
   * Returns the total value summed across all players
   * @return total
   */
  public double getTotal() {
    double total = 0;
    for(int p = _players.length - 1; p >= 0; p--) {
      total += getValue(_players[p]);
    }
    return total;
  }

  /**
   * Returns the average of the values (ie: total divided by number of players)
   * @return average
   */
  public double getAverage() {
    return getTotal() / _players.length;
  }

  /**
   * Returns the fraction of the total attributable to the specified player.
   * This will be a value between 0 and 1 inclusive.
   * @param player
   * @return percentage
   */
  public double getPercentage(String player) {
    double total = getTotal();
    if(total<=0) { 
      throw new IllegalStateException("Cannot calculate percentage when total is " + total);
    }
    double value = getValue(player);
    double percentage = value / total;
    assert(percentage >=0);
    assert(percentage <=1);
    return percentage;
  }

  /**
   * Lookup the value holder from the index map and return it. If none is found
   * for the specified player then raise an exception
   * @param player
   * @return holder
   */
  protected MutableDouble getHolder(String player) {
    MutableDouble holder = (MutableDouble) _index.get(player);
    if(holder == null) {
      throw new IllegalArgumentException("No value holder found for player " + player);
    }
    return holder;
  }
}
