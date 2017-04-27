package romeo.battle.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import romeo.battle.IBattleMetrics;
import romeo.battle.PlayerSummary;
import romeo.battle.UnitSummary;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.units.api.IUnit;
import romeo.utils.BeanComparator;
import romeo.utils.MutableDouble;
import romeo.utils.MutableDoubleListWrapper;

/**
 * Bean to hold and accumulate various metrics from the battle simulation such
 * as number of times each side wins etc...
 */
class BattleMetricsImpl implements IBattleMetrics {
  private int _numberOfBattles;
  private double _averageRounds;
  private long _time;
  private String _notes;
  private String[] _players;
  private String _defenderName;
  private MutableDoubleListWrapper _roundsAchievement;
  private List<Integer> _battleLengths = new ArrayList<Integer>();
  private Map<String, FleetContents> _fleets = new HashMap<String, FleetContents>();
  private Map<String, FleetContents> _averageSurvivors = new HashMap<String, FleetContents>();
  private Map<String, FleetContents> _adjustedAverageSurvivors = new HashMap<String, FleetContents>();
  private PlayerIndexedValues _averageSurvivingFirepower;
  private PlayerIndexedValues _adjustedAverageSurvivingFirepower;
  private PlayerIndexedValues _wins;
  private PlayerIndexedListMap _firepower;
  private PlayerIndexedListMap _adjustedFirepower;
  private PlayerIndexedListMap _populationDamage;
  private PlayerIndexedListMap _adjustedPopulationDamage;

  /**
   * Constructor. The names of players are required to initialise the internal
   * structures.
   * @param players
   */
  public BattleMetricsImpl(String[] players, String defenderName, int numberOfBattles) {
    _players = players;
    _defenderName = defenderName;
    _numberOfBattles = numberOfBattles;
    _roundsAchievement = new MutableDoubleListWrapper();
    _battleLengths = new ArrayList<Integer>(numberOfBattles);
    _averageSurvivingFirepower = new PlayerIndexedValues(players);
    _adjustedAverageSurvivingFirepower = new PlayerIndexedValues(players);
    _wins = new PlayerIndexedValues(players);

    _firepower = new PlayerIndexedListMap(players);
    _adjustedFirepower = new PlayerIndexedListMap(players);
    _populationDamage = new PlayerIndexedListMap(players);
    _adjustedPopulationDamage = new PlayerIndexedListMap(players);

    for(int p = players.length - 1; p >= 0; p--) {
      _averageSurvivors.put(players[p], new FleetContents());
      _adjustedAverageSurvivors.put(players[p], new FleetContents());
    }
  }

  public void calculateAverages(int sumOfRounds) {
    //convert fp and pd lists to averages
    //nb: we iterate from round 1 as already averaged stuff for pre-battle round zero
    setAverageRounds((double) sumOfRounds / (double) _numberOfBattles);

    int totalRoundsRecorded = getRecordedRoundCount();
    for(int p = _players.length - 1; p >= 0; p--) {
      String player = _players[p];
      for(int roundIndex = 1; roundIndex < totalRoundsRecorded; roundIndex++) { //Loop over all the rounds in the fp and pd tables and convert to averages
        int battlesThatGotThisFar = getRoundAchievement(roundIndex);

        double totalFirepower = getFirepower(player, false, roundIndex);
        setFirepower(player, false, roundIndex, totalFirepower / _numberOfBattles);
        setFirepower(player, true, roundIndex, totalFirepower / battlesThatGotThisFar);

        double totalPd = getPopulationDamage(player, false, roundIndex);
        setPopulationDamage(player, false, roundIndex, totalPd / _numberOfBattles);
        setPopulationDamage(player, true, roundIndex, totalPd / battlesThatGotThisFar);
      }

      double battlesWonByThisPlayer = getWinCount(player);

      FleetContents avgSurvivors = getAverageSurvivors(player, false);
      FleetContents adjustedAvgSurvivors = new FleetContents(avgSurvivors); //Create copy for adjusted figures
      setAverageSurvivors(player, true, adjustedAvgSurvivors); //And store

      //Divide each set of average suvivors by total number of battles to get the raw average
      avgSurvivors.divideQuantity(_numberOfBattles);
      avgSurvivors.normalise(true);
      avgSurvivors.sort();

      //For adjusted average number of survivors we divide by the number of battles player won
      if(battlesWonByThisPlayer > 0) { //We cant divide if divisor is zero - in which case nothing to divide anyway
        adjustedAvgSurvivors.divideQuantity(battlesWonByThisPlayer);
      } else {
        if(adjustedAvgSurvivors.getFirepower() > 0) { //Sanity check
          throw new IllegalStateException("player won zero battles yet had surviving firepower");
        }
      }
      adjustedAvgSurvivors.normalise(true);
      adjustedAvgSurvivors.sort();

      double totalSurvivingFp = getAverageSurvivingFirepower(player, false);
      setAverageSurvivingFirepower(player, false, totalSurvivingFp / (double) _numberOfBattles);

      double adjustedAverageSurvivingFp = battlesWonByThisPlayer > 0 ? totalSurvivingFp / battlesWonByThisPlayer : 0;
      setAverageSurvivingFirepower(player, true, adjustedAverageSurvivingFp);
    }
  }

  /**
   * Create and return an instance of {@link PlayerSummary} for the specified
   * player.
   * @param player
   * @return summary
   */
  @Override
  public PlayerSummary createSummary(String player) {
    PlayerSummary summary = new PlayerSummary();
    summary.setName(player);
    summary.setWinCount(getWinCount(player));
    summary.setWinPercent(getWinsPercentage(player) * 100d);
    summary.setAverageSurvivingFirepower(getAverageSurvivingFirepower(player, false));
    summary.setAdjustedAverageSurvivingFirepower(getAverageSurvivingFirepower(player, true));
    summary.setSurvivors(createSurvivorsSummary(player));
    return summary;
  }

  protected List<UnitSummary> createSurvivorsSummary(String player) {
    Map<IUnit, UnitSummary> smap = new HashMap<IUnit, UnitSummary>();
    FleetContents initialFleet = getFleet(player);
    FleetContents averageSurvivors = getAverageSurvivors(player, false);
    FleetContents adjustedAverageSurvivors = getAverageSurvivors(player, true);
    addUnitsToSummary(initialFleet, smap, "initialQuantity");
    addUnitsToSummary(averageSurvivors, smap, "averageSurvivors");
    addUnitsToSummary(adjustedAverageSurvivors, smap, "adjustedAverageSurvivors");
    List<UnitSummary> survivors = new ArrayList<UnitSummary>(smap.values());
    Collections.sort(survivors, new BeanComparator("unit.name"));
    return survivors;
  }

  /**
   * Internal method used by createSummary to add info to the map it is
   * building. The method will make a normalised-flat copy of the fleet.
   * Instances of UnitSUmmary are created in the map as needed and indexed by
   * the unit object.
   * @param fleet
   * @param map
   * @param property
   *          name of property to set with qty
   */
  private void addUnitsToSummary(FleetContents fleet, Map<IUnit, UnitSummary> map, String property) {
    FleetContents flat = new FleetContents(fleet);
    flat.normalise(true);
    for(FleetElement fleetElement : fleet) {
      IUnit unit = fleetElement.getUnit();
      UnitSummary unitSummary = (UnitSummary) map.get(unit);
      if(unitSummary == null) {
        unitSummary = new UnitSummary();
        unitSummary.setUnit(unit);
        map.put(unit, unitSummary);
      }

      //Following replaces some reflections basecd code that didnt like the int
      if("initialQuantity".equals(property)) {
        unitSummary.setInitialQuantity((int) fleetElement.getQuantity());
      } else if("averageSurvivors".equals(property)) {
        unitSummary.setAverageSurvivors(fleetElement.getQuantity());
      } else if("adjustedAverageSurvivors".equals(property)) {
        unitSummary.setAdjustedAverageSurvivors(fleetElement.getQuantity());
      }

    }
  }

  /**
   * Create a summary for each player and return these as a list of
   * {@link PlayerSummary}.
   * @return summary List of PlayerSummary
   */
  @Override
  public List<PlayerSummary> createSummary() {
    String[] players = getPlayers();
    List<PlayerSummary> summary = new ArrayList<PlayerSummary>(players.length);
    for(int p = 0; p < players.length; p++) { //Add summary for each player into the list in same order as found in players array
      summary.add(createSummary(players[p]));
    }
    return summary;
  }

  @Override
  public int getRecordedRoundCount() {
    return getFirepower(getDefendingPlayer(), false).size();
  }

  @Override
  public String[] getPlayers() {
    return _players;
  }

  @Override
  public String getDefendingPlayer() {
    return _defenderName;
  }

  @Override
  public FleetContents getFleet(String player) {
    FleetContents fleet = (FleetContents) _fleets.get(player);
    if(fleet == null) {
      throw new IllegalArgumentException("No fleet recorded for " + player);
    }
    return fleet;
  }

  public void setFleet(String player, FleetContents fleet) {
    _fleets.put(player, fleet);
  }

  public void setAverageSurvivors(String player, boolean adjusted, FleetContents fleet) {
    Map<String, FleetContents> table = adjusted ? _adjustedAverageSurvivors : _averageSurvivors;
    table.put(player, fleet);
  }

  @Override
  public FleetContents getAverageSurvivors(String player, boolean adjusted) {
    Map<String, FleetContents> table = adjusted ? _adjustedAverageSurvivors : _averageSurvivors;
    FleetContents fleet = (FleetContents) table.get(player);
    if(fleet == null) {
      String a = adjusted ? "adjusted average survivors" : "average survivors";
      throw new IllegalArgumentException("No " + a + " recorded for " + player);
    }
    return fleet;
  }

  /**
   * Used in addSurvivors to keep track of how many survivor fleets have been
   * observed. Some cleanup is performed every so often based on this.
   */
  private int _observationCounter = 0;

  /**
   * Adds a survivors fleet to their accumulated survivors and also adds the
   * total surviving firepower to the average surviving firepower counter. Note
   * that this method might not add to the adjusted averages, these are
   * calculated based on the same totals later. Ie: until the calculateAverages
   * method is called the values returned from the getters are not fully
   * prepared.
   * @param player
   * @param survivingFleet
   */
  public void addSurvivors(String player, FleetContents survivingFleet) {
    FleetContents countFleet = (FleetContents) _averageSurvivors.get(player);
    countFleet.addFleet(survivingFleet, true);
    _averageSurvivingFirepower.addValue(player, survivingFleet.getFirepower());
    _observationCounter++;
    if(_observationCounter % 4096 == 0) { //Cleanup all the average survivor accumuator fleets every so often
      for(int p = _players.length - 1; p >= 0; p--) { //Call normalise for each survivor accumulator fleet
        ((FleetContents) _averageSurvivors.get(_players[p])).normalise(true);
      }
      //Encourage a garbage collection to deal with the many empty FleetElement that will
      //result from the normalisations.
      System.gc();
    }
  }

  /**
   * Returns the raw count of wins recorded for the specified player. To
   * determine the number of ties pass null for the player name (it will be
   * calculated by subtracting the total number of wins of all the players from
   * the total number of battles actually simulated)
   * @param player
   *          or null to calculate count of ties
   * @return number of wins recorded for specified player
   */
  @Override
  public int getWinCount(String player) {
    if(player == null) {
      //The wins object doesnt hold ties, so we need to calculate ourselves
      return _numberOfBattles - (int) _wins.getTotal();
    } else {
      return (int) _wins.getValue(player);
    }
  }

  /**
   * Adds a win for the specified player. If player is null this indicates a tie
   * and no action is taken (as the number of ties will be calculated on demand
   * as the difference between the total number of wins recorded and the number
   * of battles actually simulated)
   * @param player
   */
  public void addWin(String player) {
    if(player != null) {
      _wins.addValue(player, 1);
    }
  }

  /**
   * Returns the percentage of total wins recorded the specified player across
   * all the battles simulated. To find the percentage attributable to ties pass
   * null for the player.
   * @param player
   *          player name or null
   * @return percentage of wins
   */
  @Override
  public double getWinsPercentage(String player) {
    return (double) getWinCount(player) / _numberOfBattles;
  }

  /**
   * Returns the entire firepower data list for the specified player
   * @param player
   * @param adjusted
   * @return firepowerDataByRound
   */
  @Override
  public List<? extends Number> getFirepower(String player, boolean adjusted) {
    PlayerIndexedListMap firepower = adjusted ? _firepower : _adjustedFirepower;
    return firepower.getData(player);
  }

  public double getFirepower(String player, boolean adjusted, int round) {
    PlayerIndexedListMap firepower = adjusted ? _firepower : _adjustedFirepower;
    return firepower.getValue(player, round);
  }

  /**
   * Set the firepower for the specified player and round to the specified
   * explicit value.
   * @param player
   * @param adjusted
   *          set the adjusted firepower (if false sets raw fp stat)
   * @param round
   *          the index
   * @param fp
   *          the value to be set
   */
  public void setFirepower(String player, boolean adjusted, int round, double fp) {
    PlayerIndexedListMap firepower = adjusted ? _firepower : _adjustedFirepower;
    firepower.setValue(player, round, fp);
  }

  /**
   * Add the deltaFp to the value already in the firepower data for the
   * specified round and player. If there is no data recorded for that round yet
   * then will create necessary object to store the value specified.
   * @param player
   * @param adjusted
   *          set the adjusted firepower (if false sets raw fp stat)
   * @param round
   *          the index
   * @param deltaFp
   *          the amount to add to the existing fp value
   */
  public void addFirepower(String player, boolean adjusted, int round, double deltaFp) {
    PlayerIndexedListMap firepower = adjusted ? _firepower : _adjustedFirepower;
    firepower.addValue(player, round, deltaFp);
  }

  public double getPopulationDamage(String player, boolean adjusted, int round) {
    PlayerIndexedListMap populationDamage = adjusted ? _populationDamage : _adjustedPopulationDamage;
    return populationDamage.getValue(player, round);
  }

  @Override
  public List<? extends Number> getPopulationDamage(String player, boolean adjusted) {
    PlayerIndexedListMap populationDamage = adjusted ? _populationDamage : _adjustedPopulationDamage;
    return populationDamage.getData(player);
  }

  public void setPopulationDamage(String player, boolean adjusted, int round, double pd) {
    PlayerIndexedListMap populationDamage = adjusted ? _populationDamage : _adjustedPopulationDamage;
    populationDamage.setValue(player, round, pd);
  }

  public void addPopulationDamage(String player, boolean adjusted, int round, double deltaPd) {
    PlayerIndexedListMap populationDamage = adjusted ? _populationDamage : _adjustedPopulationDamage;
    populationDamage.addValue(player, round, deltaPd);
  }

  @Override
  public double getAverageSurvivingFirepower(String player, boolean adjusted) {
    PlayerIndexedValues sfp = adjusted ? _adjustedAverageSurvivingFirepower : _averageSurvivingFirepower;
    return sfp.getValue(player);
  }

  public void setAverageSurvivingFirepower(String player, boolean adjusted, double fp) {
    PlayerIndexedValues sfp = adjusted ? _adjustedAverageSurvivingFirepower : _averageSurvivingFirepower;
    sfp.setValue(player, fp);
  }

  /**
   * String representation of this object for use in debugging. Not suitable for
   * display to user.
   * @return string
   */
  @Override
  public String toString() {
    return "BattleMetrics: numberOfBattles=" + _numberOfBattles;
  }

  @Override
  public int getNumberOfBattles() {
    return _numberOfBattles;
  }

  public void setNumberOfBattles(int battleCount) {
    _numberOfBattles = battleCount;
  }

  @Override
  public double getAverageRounds() {
    return _averageRounds;
  }

  public void setAverageRounds(double rounds) {
    _averageRounds = rounds;
  }

  @Override
  public long getTime() {
    return _time;
  }

  public void setTime(long millis) {
    _time = millis;
  }

  /**
   * Returns a sorted copy of the battle length observations
   * @return battleLengths
   */
  @Override
  public List<? extends Number> getBattleLengths() {
    ArrayList<Integer> list = new ArrayList<Integer>(_battleLengths);
    Collections.sort(list);
    return list;
  }

  /**
   * Adds a battle length obvservation.
   * @param rounds
   *          number of rounds in the battle
   */
  public void addBattleLength(int rounds) {
    _battleLengths.add(new Integer(rounds));
  }

  /**
   * Returns a list containing the number of battles that achieved each round.
   * Their is one element for each round, indexed by the round number. The
   * element contains a {@link MutableDouble} holding the number of battles that
   * achieved that round (including those that went on to have more rounds). The
   * list returned is intended for read only use. Its size will be equal to the
   * number of rounds in the longest battle.
   * @return roundAchievement
   */
  @Override
  public List<? extends Number> getRoundAchievement() {
    return _roundsAchievement.getData();
  }

  /**
   * Returns the number of battles reaching or surpassing the specified round.
   * The round number may be larger than the largest battle (in which case zero
   * is returned as no battles got that far)
   * @param round
   * @return battles
   */
  @Override
  public int getRoundAchievement(int round) {
    if(_roundsAchievement.getSize() > round) {
      return (int) _roundsAchievement.getValue(round);
    } else {
      return 0;
    }
  }

  /**
   * Increment the count of the number of battles that achieved (including those
   * that surpassed) the specified round
   * @param round
   */
  public void addRoundAchievement(int round) {
    _roundsAchievement.addValue(round, 1);
  }

  /**
   * Set the battle count for a specified round to an explicit number of battles
   * that achieved that round (include in the count that that went on to achieve
   * more rounds)
   * @param round
   * @param count
   */
  public void setRoundAchievement(int round, int count) {
    _roundsAchievement.setValue(round, count);
  }

  @Override
  public String getNotes() {
    return _notes;
  }

  public void setNotes(String notes) {
    _notes = notes;
  }

}
