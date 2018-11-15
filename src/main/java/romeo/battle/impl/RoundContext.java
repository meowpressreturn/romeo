package romeo.battle.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.xfactors.api.IXFactorCompiler;

/**
 * Mutable bean to wrap various items of interest to xfactors (and other classes) during
 * a round of battle. Take note that not all properties are available at all
 * times during the progression of the round logic.
 */
public class RoundContext {
  /**
   * SourceId in the defender fleet that represents the base fleet (0)
   */
  public static final int BASE_SOURCE = 0;

  protected String _defendingPlayer;
  protected String _thisPlayer;
  protected int _round;
  protected FleetElement _fleetElement;
  protected IXFactorCompiler _compiler;

  protected StringBuffer _roundLog = new StringBuffer();

  protected Map<String, FleetContents> _fleets = new LinkedHashMap<String, FleetContents>();
  protected String[] _players;
  protected FleetContents[] _opposingFleets; //calculated in setDefendingPlayer
  protected String[] _opposingPlayers; //calculated in setDefendingPlayer

  /**
   * No-args constructor. Round number will be initialised to zero. Other
   * properties require setting.
   */
  public RoundContext(String[] players) {
    setRound(0);
    _players = players;
  }

  public void setFleet(String player, FleetContents fleet) {
    _fleets.put(player, fleet);
  }

  public FleetContents getFleet(String player) {
    FleetContents fleet = (FleetContents) _fleets.get(player);
    if(fleet == null) {
      throw new IllegalArgumentException("No fleet for player: " + player);
    }
    return fleet;
  }

  /**
   * Comments show up in the battle report for the first few battles simulated.
   * @param comment
   *          to add
   */
  public void addComment(String comment) {
    _roundLog.append(comment);
  }

  /**
   * Clear the comments
   */
  public void clearComments() {
    _roundLog = new StringBuffer();
  }

  /**
   * Returns the comments
   * @return comments
   */
  public String getComments() {
    return _roundLog.toString();
  }

  /**
   * Gets the defenders fleet
   * @return defender
   */
  public FleetContents getDefender() {
    return getFleet(_defendingPlayer);
  }

  /**
   * Get the round number. Round zero is pre-battle conditions.
   * @return round
   */
  public int getRound() {
    return _round;
  }

  /**
   * Returns the fleet to which the current unit belongs
   * @return fleet
   */
  public FleetContents getThisFleet() {
    return getFleet(_thisPlayer);
  }

  /**
   * Returns true if this unit belongs to an attacker
   * @param isAttacker
   *          returns true if not a defender
   */
  public boolean isAttacker() {
    return !isDefender();
  }

  /**
   * Returns true if this unit belongs to the defender
   * @return isDefender
   */
  public boolean isDefender() {
    return _thisPlayer.equals(_defendingPlayer);
  }

  /**
   * Returns the opponent fleets
   * @return opposingFleets
   */
  public FleetContents[] getOpposingFleets() {
    if(_opposingFleets.length != _fleets.size() - 1) {
      throw new IllegalStateException("opposing fleets array not calculated");
    }
    return _opposingFleets;
  }

  public String[] getOpposingNames() {
    if(_opposingPlayers.length != _fleets.size() - 1) {
      throw new IllegalStateException("opposing names array not calculated");
    }
    return _opposingPlayers;
  }

  /**
   * Returns the map of working fleets keyed by player name
   * @return fleets
   */
  public Map<String, FleetContents> getFleets() {
    return _fleets;
  }

  /**
   * Sets the number of the current round of battle
   * @param round
   */
  public void setRound(int round) {
    _round = round;
  }

  /**
   * Returns the reference to the current units {@link FleetElement}
   * @return element
   */
  public FleetElement getFleetElement() {
    return _fleetElement;
  }

  /**
   * Set the reference to the current units fleet element (there may be more
   * than one element in a FleetContents object for units of a particular type)
   * @param element
   */
  public void setFleetElement(FleetElement element) {
    _fleetElement = element;
  }

  public String getDefendingPlayer() {
    return _defendingPlayer;
  }

  public void setDefendingPlayer(String defendingPlayer) {
    _defendingPlayer = defendingPlayer;
  }

  public String getThisPlayer() {
    return _thisPlayer;
  }

  /**
   * Set which player owns the unit for which the xFactor is being evaluated. Note that the
   * fleet references must have been initialised first or this will fail.
   * @param thisPlayer
   */
  public void setThisPlayer(String thisPlayer) {
    _thisPlayer = thisPlayer;
    updateOpposingFleets();
  }

  /**
   * Updates the array of opposing fleets. This is basically all the fleets that
   * are not the current fleet.
   */
  protected void updateOpposingFleets() {
    String thisPlayer = _thisPlayer;
    if(_fleets.size() < 1) {
      throw new IllegalStateException("Fleets have not been initialised");
    }
    FleetContents[] opposingFleets = new FleetContents[_fleets.size() - 1];
    String[] opposingPlayers = new String[opposingFleets.length];
    Iterator<Map.Entry<String, FleetContents>> i = _fleets.entrySet().iterator();
    int j = 0;
    while(i.hasNext()) {
      Map.Entry<String, FleetContents> entry = i.next();
      String player = (String) entry.getKey();
      if(!player.equals(thisPlayer)) {
        opposingFleets[j] = (FleetContents) entry.getValue();
        opposingPlayers[j] = player;
        j++;
      }
    }
    _opposingFleets = opposingFleets;
    _opposingPlayers = opposingPlayers;
  }

  /**
   * Set player using the fleet reference. This is a little hacky due to change
   * in the interface. Hopefully we can refactor it to be more elegant.
   */
  public void setThisPlayer(FleetContents fleet) {
    Iterator<Map.Entry<String, FleetContents>> i = _fleets.entrySet().iterator();
    while(i.hasNext()) { //Compare references to find our fleet in order to work out what its name is
      Map.Entry<String, FleetContents> entry = i.next();
      if(fleet == entry.getValue()) {
        String playerName = (String) entry.getKey();
        setThisPlayer(playerName);
        return;
      }
    }
    throw new IllegalArgumentException("Fleet not found in round context (are fleets set yet?)");
  }

}
