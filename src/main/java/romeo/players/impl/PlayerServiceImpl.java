package romeo.players.impl;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import romeo.model.api.InvalidTurnException;
import romeo.persistence.AbstractPersistenceService;
import romeo.persistence.DuplicateRecordException;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerId;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;

/**
 * Manages the persistence and retrieval of player information. The
 * {@link IPlayer} interface defines getters for properties that represent the
 * information about a player.
 */
public class PlayerServiceImpl extends AbstractPersistenceService implements IPlayerService {
  
  private List<IPlayer> _playersCache;
  private Map<PlayerId, IPlayer> _playersById; 
  private Map<String, IPlayer> _playersByName;

  /**
   * Constructor
   * @param dataSource
   *          required
   * @param keyGen
   *          required
   */
  public PlayerServiceImpl(DataSource dataSource, IKeyGen keyGen) {
    super(dataSource, keyGen);
  }

  /**
   * Returns a list of all players using the cached data (initialising the cache if
   * necessary)
   * @return players
   */
  @Override
  public synchronized List<IPlayer> getPlayers() {
    if(!cacheInitialised()) {
      initCache();
    }
    return Collections.unmodifiableList(_playersCache);
  }

  /**
   * Returns summary data about players for a given turn. This method retrieves its data directly from the
   * database and does not use the cache.
   * The summary takes the form of a List of Maps. Each element in the list is information for one
   * player. Keys include: id, name, status, notes, color, team, worlds, visibleFp
   * An exception is thrown if you request a negative or 0 turn number, but any positive number is fine even if
   * there is no history data for that turn (it will return 0 for such missing historical values)
   * @return summary
   */
  @Override
  public synchronized List<Map<String, Object>> getPlayersSummary(int turn) {
    if(turn<1) {
      throw new InvalidTurnException(turn);
    }
    List<Map<String, Object>> results = new ArrayList<>();
    try(Connection connection = _dataSource.getConnection()) {
//      String sql = "SELECT p.id AS id, p.name AS name, p.status AS status, p.notes AS notes,"
//          + " p.color AS color, p.team AS team, COALESCE(worlds,0) AS worlds, COALESCE(visibleFp,0) AS visibleFp,"
//          + " COALESCE(labour,0) AS labour, COALESCE(capital,0) AS capital"
//          + " FROM players p LEFT JOIN"
//          + " (SELECT owner, COUNT(*) AS worlds, SUM( firepower) AS visibleFp, SUM(labour) AS labour, SUM(capital) AS capital"
//          + " FROM worlds_history WHERE turn=? GROUP BY owner) wh ON p.name=wh.owner";
      final String sql
          = "SELECT p.id AS id, p.name AS name, p.status AS status, p.notes AS notes,"
          + " p.color AS color, p.team AS team, COALESCE(worlds,0) AS worlds, COALESCE(visibleFp,0) AS visibleFp,"
          + " COALESCE(labour,0) AS labour, COALESCE(capital,0) AS capital"
          + " FROM players p LEFT JOIN"
          + " (SELECT UCASE(owner) AS ownerLookup, COUNT(*) AS worlds, SUM( firepower) AS visibleFp, SUM(labour) AS labour, SUM(capital) AS capital"
          + " FROM worlds_history WHERE turn=? GROUP BY owner) wh ON p.nameLookup=wh.ownerLookup";
      ResultSet rs = DbUtils.readQuery(sql, new Object[] { turn }, connection);
      while(rs.next()) {
        Map<String, Object> player = new TreeMap<String, Object>();
        DbUtils.readColumns(rs, player, "id", "name", "status", "notes", "color", "team", "worlds", "visibleFp",
            "labour", "capital");
        player.put("color", Convert.toColor((String) player.get("color"))); //Color string needs to be converted to a Color
        player.put("id", new PlayerId((String)player.get("id"))); //Since 0.6.3 we use id types
        results.add(player);
      }
      return results;
    } catch(Exception e) {
      throw new RuntimeException("Error performing players summary query ", e);
    }
  }

  /**
   * Adds the record to the list of players and saves it to the db or updates an
   * existing row in the db. Informs any listeners on the PlayerService that
   * data has changed. The id allocated for the player record by Romeo is
   * returned.
   * @param player
   */
  @Override
  public synchronized PlayerId savePlayer(IPlayer player) {
    Objects.requireNonNull(player, "player may not be null");
    checkDuplicate(player);
    return savePlayerInternal(player, true);
  }

  /**
   * Load a player using the record id
   * @param id
   * @return player
   */
  @Override
  public synchronized IPlayer loadPlayer(PlayerId id) {
    Objects.requireNonNull(id, "id may not be null");
    //nb: as of 0.6.3 we don't hit the db for individual player records anymore but rather fetch them
    //from the cache, initialising it if necessary
    if(!cacheInitialised()) {
      initCache();
    }
    return _playersById.get(id);
  }

  /**
   * Load a player record given the player name. 
   * As of 0.6.3 this is now case insensitive
   * @param name
   * @return player
   */
  @Override
  public synchronized IPlayer loadPlayerByName(String name) {
    Objects.requireNonNull(name, "name may not be null");
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    name = name.trim().toUpperCase(Locale.US);
    //nb: as of 0.6.3 we don't hit the db for individual player records anymore but rather fetch them
    //from the cache, initialising it if necessary
    if(!cacheInitialised()) {
      initCache();
    }
    return _playersByName.get(name);
  }

  /**
   * Remove a player from the db given the id and notify listeners
   * @param id
   */
  @Override
  public synchronized void deletePlayer(PlayerId id) {
    Objects.requireNonNull(id, "id may not be null");
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "DELETE FROM PLAYERS WHERE id=?;";
      Object[] parameters = new Object[] { id };
      DbUtils.writeQuery(sql, parameters, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error deleting player " + id, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  /**
   * Save/update multiple player records as a batch with a single data change notification after all
   * have been saved. (nb: if the supplied collection is empty, no notification is sent)
   * @param players
   */
  @Override
  public synchronized void savePlayers(Collection<IPlayer> players) {
    Objects.requireNonNull(players, "players must not be null");
    if(!players.isEmpty()) {
      
      //Since 0.6.3 we enforce the uniqueness of player names (case-insensitive)
      Set<String> names = new HashSet<>();
      for(IPlayer player : players) {
        Objects.requireNonNull(player.getName(), "player.name may not be null");
        String name = player.getName().trim().toUpperCase(Locale.US);
        if(names.contains(name)) {
          throw new DuplicateRecordException("Duplicated player name in save list: " + name);
        }
      }
      
      for(IPlayer player : players) {
        checkDuplicate(player);
        savePlayerInternal(player, false);
      }
      notifyDataChanged();
    }
  }

  private boolean cacheInitialised() {
    return _playersCache != null;
  }
  
  /**
   * Clear all the cached information
   */
  private synchronized void flushCache() {
    _playersCache = null;
    _playersById = null;
    _playersByName = null;
  }

  /**
   * Initialise the cache from the database.
   */
  private synchronized void initCache() {
    List<IPlayer> results = new ArrayList<>();
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "SELECT id,name,status,notes,color,team" + " FROM PLAYERS ORDER BY UCASE(name) ASC";
      ResultSet rs = DbUtils.readQuery(sql, null, connection);
      while(rs.next()) {
        results.add(readPlayer(rs));
      }
    } catch(Exception e) {
      throw new RuntimeException("Error loading players ", e);
    }    
    _playersCache = Collections.unmodifiableList(results);
    _playersById = new HashMap<PlayerId, IPlayer>();
    _playersByName = new HashMap<String, IPlayer>();
    for(IPlayer player : _playersCache) {
      _playersById.put(player.getId(), player);
      String name = player.getName();
      if(name != null && !name.isEmpty()) { 
        name = name.toUpperCase(Locale.US);
        //nb: we only store the first one with that name, so if users duplicate them its a problem
        if(!_playersByName.containsKey(name)) {
          _playersByName.put(name, player);
        }
      }
    }
  }
  
  /**
   * Insert or update the database row for the player. This method will cause
   * the cache to be flushed. The id allocated for the player is returned. If
   * the notify flag is set then listeners to the service will be notified.
   * @param player
   * @param notify
   * @return id
   */
  private PlayerId savePlayerInternal(IPlayer player, boolean notify) {
    Objects.requireNonNull(player, "player may not be null");
    if(player.getName().isEmpty()) {
      throw new IllegalArgumentException("player.name may not be empty");
    }
    String sql = null;
    Object[] parameters = null;
    PlayerId id;
    try(Connection connection = _dataSource.getConnection()) {
      if(player.isNew()) {
        id = new PlayerId(_keyGen.createIdKey() );
        sql = "INSERT INTO PLAYERS (id,name,status,notes,color,team) VALUES (?,?,?,?,?,?);";
        parameters = new Object[] { id, player.getName(), player.getStatus(), player.getNotes(),
            Convert.toStr(player.getColor()), player.getTeam(), };
      } else {
        id = player.getId();
        sql = "UPDATE PLAYERS SET name=?,status=?,notes=?,color=?,team=? WHERE id=?;";
        parameters = new Object[] { player.getName(), player.getStatus(), player.getNotes(),
            Convert.toStr(player.getColor()), player.getTeam(), id, };
      }
      DbUtils.writeQuery(sql, parameters, connection);
      return id;
    } catch(Exception e) {
      throw new RuntimeException("Error saving player " + player, e);
    } finally {
      flushCache();
      if(notify) {
        notifyDataChanged();
      }
    }
  }
  
  /**
   * Read player record from a ResultSet row
   * @param rs
   * @return player
   */
  private IPlayer readPlayer(ResultSet rs) {
    Objects.requireNonNull(rs, "rs may not be null");
    try {
      PlayerId id = new PlayerId( rs.getString("id") );
      String name = rs.getString("name");
      String status = rs.getString("status");
      String notes = rs.getString("notes");
      String colorString = rs.getString("color");
      String team = rs.getString("team");
      Color color = Convert.toColor(colorString);
      PlayerImpl player = new PlayerImpl(id, name, status, notes, color, team);
      return player;
    } catch(Exception e) {
      throw new RuntimeException("Error reading row from result set", e);
    }
  }
  
  private void checkDuplicate(IPlayer player) {
    Objects.requireNonNull(player, "unit may not be null");
    Objects.requireNonNull(player.getName(), "player.name may not be null");
    IPlayer other = loadPlayerByName( player.getName() );
    if(other != null && !other.getId().equals(player.getId())) {
      throw new DuplicateRecordException("Player name may not be the same as an existing player: " + player.getName());
    }
  }

}
