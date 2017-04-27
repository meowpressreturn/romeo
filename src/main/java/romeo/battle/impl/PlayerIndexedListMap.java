//Created 2008-11-22
package romeo.battle.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import romeo.utils.MutableDouble;
import romeo.utils.MutableDoubleListWrapper;

/**
 * Manages lists of mutable double data indexed by player name. This is
 * primarily used to manage the metrics for round by round averages (hence the
 * use of a double).
 * Maintains a list of values for each players (the size of which is the highest index written too. It will
 * initialise elements between the previous end of the list and that with zero). Attempts to read or write
 * values for players not declared to the constructor will result in an IllegalArgumentException.
 */
public class PlayerIndexedListMap {
  /**
   * Interface for visitors that may be passed to the forEach method
   */
  public static interface IVisitor {
    /**
     * Visit a data item
     * @param player
     * @param index
     * @param holder
     *          the value holder object
     */
    public void visit(String player, int index, MutableDouble holder);
  }

  /**
   * Visitor implementation that will divide the value visited by a divisor
   */
  public static class DivideBy implements IVisitor {
    private double _divisor;

    /**
     * Constructor. Takes a divisor which may not be zero.
     * @param divisor
     */
    public DivideBy(double divisor) {
      setDivisor(divisor);
    }

    /**
     * Return the divisor
     * @return divisor
     */
    public double getDivisor() {
      return _divisor;
    }

    /**
     * Set the divisor. An exception is raised if it is zero.
     * @param divisor
     */
    public void setDivisor(double divisor) {
      if(divisor == 0) {
        throw new IllegalArgumentException("divisor may not be zero");
      }
      _divisor = divisor;
    }

    /**
     * Divide value by the divisor
     * @param player
     * @param index
     * @param holder
     */
    @Override
    public void visit(String player, int index, MutableDouble holder) {
      holder.divideValue(_divisor);
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  protected String[] _players;
  protected Map<String, MutableDoubleListWrapper> _index = new HashMap<String, MutableDoubleListWrapper>();

  /**
   * Constructor. Requires a valid list of players. Will validate that list is
   * not null, not empty, and that each player named is not null or an empty
   * string and does not match any player already specified.
   * @param players
   *          an array of player names
   */
  public PlayerIndexedListMap(String[] players) {
    _players = Objects.requireNonNull(players, "players may not be null");
    if(players.length < 1) {
      throw new IllegalArgumentException("No players specified");
    }
    for(int p = players.length - 1; p >= 0; p--) {
      String player = players[p];
      if(player == null) {
        throw new NullPointerException("Player name is null at index " + p);
      }
      if(player.isEmpty()) {
        throw new IllegalArgumentException("Player name not specified at index " + p);
      }
      if(_index.containsKey(player)) {
        throw new IllegalArgumentException("Player already specified:" + player);
      }
      _index.put(player, new MutableDoubleListWrapper());
    }
  }

  /**
   * NOT IMPLEMENTED YET
   * Ensure that all the lists are the same length by expanding the smaller ones
   * with zero values.
   */
  public void syncListSizes() {
    throw new UnsupportedOperationException("This has yet to be implemented");
  }

  /**
   * Invoke the visitors visit() method for each element of each players lists
   * of data. The order in which the players are iterated is not specified.
   * Their data lists will be iterated from first to last element.
   * @param visitor
   */
  public void forEach(IVisitor visitor) {
    for(Map.Entry<String, MutableDoubleListWrapper> entry : _index.entrySet()) { //Visit the list for each player
      String player = entry.getKey();
      MutableDoubleListWrapper mdlw = entry.getValue();
      List<MutableDouble> data = mdlw.getData();
      int size = data.size();
      for(int i = 0; i < size; i++) { //Visit each element in list and invoke visitor
        MutableDouble holder = (MutableDouble) data.get(i);
        visitor.visit(player, i, holder);
      }
    }
  }

  /**
   * Returns the entire set of data for the specified player. The list is not intended to
   * be modified. The {@link MutableDouble} instances are the actual data holders.
   * @param player
   * @return data
   */
  public List<MutableDouble> getData(String player) {
    return Collections.unmodifiableList( getList(player).getData() );
  }

  /**
   * Returns an iterator over the list of data for the specified player. Do not
   * use the iterator to modify the data. This is intended only for reading.
   * @param player
   * @return iterator
   */
  public Iterator<MutableDouble> iterator(String player) {
    return getList(player).iterator();
  }

  /**
   * Internal method. Returns the {@link MutableDoubleListWrapper} that stores
   * data for the specified player. If the player name provided is invalid then
   * an IllegalArgumentException is raised.
   * @param player
   * @return mdlw
   */
  protected MutableDoubleListWrapper getList(String player) {
    MutableDoubleListWrapper mdlw = (MutableDoubleListWrapper) _index.get(player);
    if(mdlw == null) {
      throw new IllegalArgumentException("Unknown player " + player);
    }
    return mdlw;
  }

  public double getValue(String player, int index) {
    MutableDoubleListWrapper list = getList(player);
    return list.getValue(index);
  }

  /**
   * Set the value for the specified index to the value specified
   * @param player
   *          the player name
   * @param index
   *          the index
   * @param value
   */
  public void setValue(String player, int index, double value) {
    getList(player).setValue(index, value);
  }

  /**
   * Increment the existing value for the specified index by the specified delta
   * @param player
   *          the player name
   * @param index
   *          the index
   * @param delta
   */
  public void addValue(String player, int index, double delta) {
    getList(player).addValue(index, delta);
  }
}
