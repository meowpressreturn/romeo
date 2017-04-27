package romeo.battle;

import java.util.List;

import romeo.fleet.model.FleetContents;

/**
 * Interface providing access to battle metrics for reporting simulation
 * results.
 */
public interface IBattleMetrics {
  /**
   * Returns the approximate number of milliseconds taken to run the simulation
   * @return time
   */
  public long getTime();

  /**
   * Returns the text notes that were made by the battle calculator during the
   * simulation.
   * @return notes
   */
  public String getNotes();

  /**
   * Returns the number of battles that were simulated
   * @return battles
   */
  public int getNumberOfBattles();

  /**
   * Returns the average number of rounds taken to complete a battle
   * @return averageRounds
   */
  public double getAverageRounds();

  /**
   * Returns a list containing the length each battle lasted in rounds. The list
   * is sorted from shortest to longest. The list returned is a copy and may be
   * safely modified without affecting the metrics object.
   * @return battleLengths
   */
  public List<? extends Number> getBattleLengths();

  /**
   * Returns a List of the Number of battles with at least as many rounds as the
   * index. IE: index 3 contains a number indicating how many of the simulated
   * battles lasted 3 or more rounds.
   * @return roundAchievement
   */
  public List<? extends Number> getRoundAchievement();

  /**
   * Returns the number of battles that achieved the specified round or more
   * @param round
   * @return battles
   */
  public int getRoundAchievement(int round);

  /**
   * Returns an array containing the names of the players present in the battle.
   * Individual names may be passed to the various getters that require one.
   * @return players
   */
  public String[] getPlayers();

  /**
   * Returns the name of the defending player. All other players are considered
   * attackers. The defending players source 0 in fleet elements is the base
   * fleet.
   * @return defendingPlayer
   */
  public String getDefendingPlayer();

  /**
   * Factory method that will create a bean holding information about the
   * specific player copied from this metrics object.
   * @param player
   * @return summary
   */
  public PlayerSummary createSummary(String player);

  /**
   * Create a summary for each player and return these as a list of
   * {@link PlayerSummary}.
   * @return summary List of PlayerSummary
   */
  public List<PlayerSummary> createSummary();

  /**
   * Returns the input fleet for the specified player. This contains all the
   * units that that side had at the battle that was simulated. The sourceId
   * within fleet elements may be used to differentiate between units coming
   * from different source fleets owned by that player.
   * @param player
   * @return fleet
   */
  public FleetContents getFleet(String player);

  /**
   * Returns the average number of survivors for the specified players. The
   * fleet returned contains fractional numbers of units, representing the
   * average numbers of those units belonging to the specified player that were
   * present after the completion of the battle. These figures may include
   * battles that the specified player lost. If the adjusted flag is set then
   * the fleet returned is an average based only on those battles the player
   * actually won as opposed to all battles.
   * @param player
   * @param adjusted
   * @return averageSurvivors a fleet containing averages of survivng units
   */
  public FleetContents getAverageSurvivors(String player, boolean adjusted);

  /**
   * Returns the fraction of battle wins that occured for the specified player
   * during the simulation. Pass null to obtain the percentage of battles that
   * were tied (ie: all units eliminated). The value returned is between 0 and 1
   * inclusive.
   * @param player
   *          player key or null for ties
   * @return win
   */
  public double getWinsPercentage(String player);

  /**
   * Returns the raw number of battles won by the specified player during the
   * simulation Specify null to get the raw number of ties.
   * @param player
   *          the player name or null
   * @return rawWin
   */
  public int getWinCount(String player);

  /**
   * Records the highest number of rounds for which round average data is
   * recorded. This value gives the size of the lists returned by getFirepower()
   * and getPopulationDamage() etc..
   * @return recordedRoundCount
   */
  public int getRecordedRoundCount();

  /**
   * Returns a List indexed by round number of the average firepowers of the
   * specified player. If set, the adjusted flag indicates to return an adjusted
   * average rather than a raw average. The adjusted average only divides by the
   * number of battles that reached that round.
   * @param player
   * @param adjusted
   * @return firepower
   */
  public List<? extends Number> getFirepower(String player, boolean adjusted);

  /**
   * Returns the average surviving post battle firepower. If the adjusted flag
   * is set it indicates to return the adjusted surviving firepower, which means
   * the average firepower for battles the specified player won, ignoring those
   * they lost or that were tied.
   * @param player
   * @param adjusted
   * @return avgSurvivingFp
   */
  public double getAverageSurvivingFirepower(String player, boolean adjusted);

  /**
   * Returns the average population damage indexed by round for the specified
   * player. If the adjusted flag is set then the average is based on the number
   * of battle reaching that round, otherwise it is based on the total number of
   * battles.
   * @param player
   * @param adjusted
   * @return pd
   */
  public List<? extends Number> getPopulationDamage(String player, boolean adjusted);

}
