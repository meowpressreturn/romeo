package romeo.players.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import romeo.model.api.IService;

/**
 * Public interface for service that manages persistence of player information.
 * Methods that modify data will cause listeners to be invoked.
 */
public interface IPlayerService extends IService {
  /**
   * Get a lost of all the players. This returns a list of players.. If there
   * are none will return an empty list and never null.
   * @return all players for which we have records
   */
  public List<IPlayer> getPlayers();

  /**
   * Query that returns extended data on all the players, including summary
   * values calculated from information available in the world history.
   * @param turn
   *          turn for which to prepare a summary
   * @return summary
   */
  public List<Map<String, Object>> getPlayersSummary(int turn);

  /**
   * Persist information about a player. Notifies any listeners to this service.
   * @param player
   */
  public PlayerId savePlayer(IPlayer player);

  /**
   * Persists information about multiple players.
   * @param players
   */
  public void savePlayers(Collection<IPlayer> players);

  /**
   * Return information about a player.
   * @param id
   *          Romeo allocated player record id
   */
  public IPlayer loadPlayer(PlayerId id);

  /**
   * Return indformation about a player given their name (returns the first
   * player with this name if for some reason more than one matching record has
   * been persisted). If the named player is not found then null is returned.
   * Name must be specified exactly.
   * @param name
   * @return player
   */
  public IPlayer loadPlayerByName(String name);

  /**
   * Remove information for a player record from the database given its id.
   * Notifies any listeners to this service.
   * @param id
   *          Romeo player id
   */
  public void deletePlayer(PlayerId id);
}
