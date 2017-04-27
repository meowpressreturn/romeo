package romeo.worlds.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import romeo.model.api.IService;
import romeo.model.api.MapInfo;

/**
 * Public interface for the service that manages persisted world and world
 * history data. Methods that modify or create worlds will cause listeners to
 * this service to be notified.
 */
public interface IWorldService extends IService {

  /**
   * Returns a single history summary observation for a given player and turn
   * @param ownerName name of the Player owning the world (not id)
   * @param turn
   * @return summary
   */
  public HistorySummary getSummary(String ownerName, int turn);

  /**
   * Get a list of all the worlds. This does not include any history
   * information.
   * @return worlds
   */
  public List<IWorld> getWorlds();

  /**
   * Get all the worlds and the History information associated with each for the
   * specified turn
   * @param turn
   * @return
   */
  public Set<WorldAndHistory> getWorldHistory(int turn);

  /**
   * Save a world to the database, creating or updating its row. Notifies any
   * listeners to this service. The id for the world is returned.
   * @param world
   */
  public WorldId saveWorld(IWorld world);

  /**
   * Persist a History bean
   * @param history
   */
  public void saveHistory(IHistory history);
  
  /**
   * Save the world and the history and fire a single change notification.
   * The worldId in the history must either be null or match the id of the world.
   * @param world
   * @param history
   * @return worldId
   */
  public WorldId saveWorldWithHistory(IWorld world, IHistory history);

  /**
   * Save changes to all worlds in the collection. For new worlds an id will be
   * allocated. Returns a list of {@link WorldId} of the saved worlds, the index
   * of the id in the returned list corresponds to the index of the world in the
   * list passed in.
   * @param worlds
   * @return ids
   */
  public List<WorldId> saveWorlds(List<IWorld> worlds);

  /**
   * Saves changes to all the history in the list.
   * Doesn't return any ids because history is keyed by the worldId it corresponds to.
   * @param histories
   */
  public void saveHistories(Collection<IHistory> histories);

  /**
   * Load a world from the database given its id.
   * @param id
   * @return world
   */
  public IWorld loadWorld(WorldId id);

  /**
   * Loads a world from the database, given its name. Note that the lookup is
   * case-insensitive.
   * @param name
   * @return
   */
  public IWorld loadWorldByName(String name);

  /**
   * Determines the extend of the map in terms of coordinates and return this as
   * a {@link MapInfo}. Also reports the highest turn for which there is history
   * recorded.
   * @return mapInfo
   */
  public MapInfo getMapInfo();

  /**
   * Remove a row for a world from the database given its id. History for the
   * world will also be removed. Notifies any listeners to this service.
   * @param worldId
   */
  public void deleteWorld(WorldId worldId);

  /**
   * Returns the History object recording data for the given turn for that
   * world. If no observation is available then returns null.
   * @param worldId
   * @param turn
   * @return
   */
  public IHistory loadHistory(WorldId worldId, int turn);

  /**
   * Returns all the history for the specified world with the element index
   * corresponding to the turn of the history. The size of the list corresponds
   * to the maximum turn for which data has been recorded, and will contain
   * nulls for turns there is no data for. Note that this implies that the 0
   * element of the list is always null.
   * @param worldId
   * @return history
   */
  public List<IHistory> loadHistory(WorldId worldId);

  /**
   * Returns true if there is already any history data for the specified turn
   * @param turn
   * @return true if any history data recorded against a world for this turn
   */
  public boolean haveData(int turn);
  
  public List<Map<String, Object>> getWorldsSummary(int turn);
}
