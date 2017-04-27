package romeo.worlds.impl;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.ApplicationException;
import romeo.model.api.IServiceListener;
import romeo.model.api.InvalidTurnException;
import romeo.model.api.MapInfo;
import romeo.persistence.AbstractPersistenceService;
import romeo.persistence.DuplicateRecordException;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerUtils;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.worlds.api.HistorySummary;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.api.WorldId;

/**
 * Manages Worlds and World History information
 */
public class WorldServiceImpl extends AbstractPersistenceService implements IWorldService, IServiceListener {

  protected ISettingsService _settingsService;

  //Implementation will cache the following until notified that something has changed
  protected MapInfo _mapInfo; //records bounds of the map
  protected List<Map<WorldId, WorldAndHistory>> _data = null; //turn indexed cache of worlds by id
  protected Map<WorldId, IWorld> _worldsById = null; //lookup table of worlds keyed by worldId  
  protected Map<String, IWorld> _worldsByName = null; //lookup worlds by uppercase name

  /**
   * Constructor. It is necessary to provide a reference to the player service,
   * so the world service can add itself as a listener and know to flush its
   * data cache when the player information changes. Likewise the unit service.
   * Also listens for changes to the default scanner range from the settings
   * service
   * @param keyGen
   * @param playerService
   * @param unitService
   * @param settingsService
   */
  public WorldServiceImpl(DataSource dataSource,
                          IKeyGen keyGen,
                          IPlayerService playerService,
                          IUnitService unitService,
                          ISettingsService settingsService) {
    super(dataSource, keyGen);
    Objects.requireNonNull(playerService, "playerService must not be null");
    Objects.requireNonNull(unitService, "unitService must not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    playerService.addListener(this);
    unitService.addListener(this);
    settingsService.addListener(this);
  }

  /**
   * Returns summary information about worlds owned in the given turn for the specified player. This will read
   * directly from the database and not use the cache.
   * NOTE: the owner value is a player name and NOT a player id! (its a loose binding and can also name players
   * that don't have a corresponding player record)
   * @param owner
   * @turn
   */
  @Override
  public synchronized HistorySummary getSummary(String owner, int turn) {
    Objects.requireNonNull(owner);
    //TODO - is owner allowed to be empty?
    if(turn < 1) {
      throw new InvalidTurnException(turn);
    }
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "SELECT owner, SUM(firepower) AS totalFirepower" + ",SUM(labour) AS totalLabour"
          + ",SUM(capital) AS totalCapital" + ",COUNT(worldId) AS worldCount"
          + " FROM WORLDS_HISTORY WHERE UCASE(owner)=UCASE(?) AND turn=? GROUP BY owner";
      ResultSet rs = DbUtils.readQuery(sql, new Object[] { owner, turn }, connection);
      if( rs.next() ) {        
        String summaryOwner = rs.getString("owner"); //Has correct case while owner argument often might not
        double totalFirepower = rs.getDouble("totalFirepower");
        int totalLabour = rs.getInt("totalLabour");
        int totalCapital = rs.getInt("totalCapital");
        int worldCount = rs.getInt("worldCount");        
        HistorySummary summary = new HistorySummary(summaryOwner, totalFirepower, totalLabour, totalCapital, worldCount);
        return summary;
      } else {
        //No data for that turn
        return new HistorySummary();
      }
    } catch(SQLException e) {
      throw new RuntimeException("Failed to read summary information for " + owner, e);
    }
  }

  /**
   * Returns a list of all the worlds. Does not include history information.
   * This information is valid across all turns. The List returned is a new
   * instance and may be modified, note though that the World instances within
   * are _not_ cloned and modifications will affect the cached values.
   * @return worlds
   */
  @Override
  public synchronized List<IWorld> getWorlds() {
    if(_data == null) { //Load all the data into memory if the cache is not yet initialised
      initCache();
    }
    List<IWorld> worlds = Collections.unmodifiableList( new ArrayList<IWorld>(_worldsByName.values()) );
    return worlds;
  }

  /**
   * Bulk-save multiple worlds. Use this in preference to repeatedly calling
   * saveWorld(), as the former will notify listeners each time. This notifies
   * listeners after persisting all the changes. It will return a list of {@link WorldId}
   * with the ids of the saved worlds.
   * @param worlds
   * @return ids
   */
  @Override
  public synchronized List<WorldId> saveWorlds(List<IWorld> worlds) {
    Objects.requireNonNull(worlds, "worlds must not be null");
    if(worlds.isEmpty()) {
      return Collections.emptyList();
    }
    
    //Verify there are no duplicate names before doing any saving!
    //We take the worlds as a List, not a Set, so not only can the same world be in the list  twice but so can the
    //same name with another world. Note that World's equality check is based on identity (world.id) so merely making
    //the parameter a Set won't help us much here, and we want to keep it a List so that the importance of order
    //remains explicit (for relating the returned ids back to their input worlds).
    Set<String> names = new HashSet<>();
    for(IWorld world : worlds) { //streams would be useful here, but we are supporting jdk7 still
      Objects.requireNonNull(world.getName(), "world.name may not be null");
      String name = world.getName().trim().toUpperCase();
      if(names.contains(name)) {
        throw new DuplicateRecordException("Duplicated world name in the save list: " + name);
      }
      names.add(name);
    }
    
    try(Connection connection = _dataSource.getConnection()) {
      List<WorldId> ids = new ArrayList<>(worlds.size());
      for(IWorld world : worlds) {
        checkDuplicate(world);
        WorldId id = saveWorldInternal(connection, world);
        ids.add(id);
      }
      //We return a list of ids assigned or existing for the worlds in the same indexes as the input world.
      //(If we were to move to using a Set (which would be better really) then probably the best solution would be
      // to return a new Set of IWorld objects that have the id property set appropriately)
      return Collections.unmodifiableList(ids);
    } catch(ApplicationException ae) {
      throw ae;
    } catch(Exception e) {
      throw new RuntimeException("Error saving worlds", e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  /**
   * Adds a world to the list of worlds and saves it to the db or updates an
   * existing row in the db. Informs any listeners on the WorldService that data
   * has changed.
   * @param world
   */
  @Override
  public synchronized WorldId saveWorld(IWorld world) {
    Objects.requireNonNull(world, "world may not be null");
    checkDuplicate(world);
    try (Connection connection = _dataSource.getConnection()) {
        WorldId id = saveWorldInternal(connection, world);
        return id;
    } catch(ApplicationException ae) {
      throw ae;
    } catch(Exception e) {
      throw new RuntimeException("Error saving world " + world, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  @Override
  public synchronized void saveHistory(IHistory history) {
    Objects.requireNonNull(history, "history may not be null");
    try(Connection connection = _dataSource.getConnection()) {
      saveHistoryInternal(connection, history);
    } catch(Exception e) {
      throw new RuntimeException("Error saving history " + history, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }
  
  @Override
  public synchronized WorldId saveWorldWithHistory(IWorld world, IHistory history) {
    Objects.requireNonNull(world, "world may not be null");
    Objects.requireNonNull(history, "history may not be null");
    if(history.getWorldId()!=null && !history.getWorldId().equals(world.getId())) {
      throw new IllegalArgumentException("history does not belong to the world");
    }
    checkDuplicate(world);
    try(Connection connection = _dataSource.getConnection()) {
      WorldId worldId = saveWorldInternal(connection, world);
      history = new HistoryImpl(worldId, history);
      saveHistoryInternal(connection, history);
      return worldId;
    } catch(ApplicationException ae) {
      throw ae;
    } catch(Exception e) {
      throw new RuntimeException("Error saving world " + world + " with history " + history, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  /**
   * Return a world given its id
   * @param id
   * @return world
   */
  @Override
  public synchronized IWorld loadWorld(WorldId id) {
    Objects.requireNonNull(id, "id may not be null");
    if(!cacheInitialised()) {
      initCache();
    }
    return _worldsById.get(id);
  }

  /**
   * Returns information about the boundary coordinates of the map.
   * @return mapInfo
   */
  @Override
  public synchronized MapInfo getMapInfo() {
    if(!cacheInitialised()) {
      initCache();
    }
    return _mapInfo;
  }

  /**
   * Remove a world and its history from the db and notify listeners
   * @param id
   */
  @Override
  public synchronized void deleteWorld(WorldId id) {
    Objects.requireNonNull(id, "id may not be null");
    try(Connection connection = _dataSource.getConnection()) {
      Object[] params = new Object[] { id };
      DbUtils.writeQuery("DELETE FROM WORLDS WHERE id=?;", params, connection);
      DbUtils.writeQuery("DELETE FROM WORLDS_HISTORY WHERE worldId=?;", params, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error deleting world " + id, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  /**
   * Returns history for a specified world and turn. Will throw an InvalidTurnException
   * if you request a turn beyond the last one for which data is available (or for 0 and
   * negative turns). Null is returned for other turns for which a record is unavailable.
   * @param worldId
   * @param turn
   * @return history
   */
  @Override
  public synchronized IHistory loadHistory(WorldId worldId, int turn) {
    Objects.requireNonNull(worldId, "worldId may not be null");
    if(turn < 1) {
      throw new InvalidTurnException(turn);
    }
    if(!cacheInitialised()) {
      initCache();
    }
    if(turn >= _data.size()) {
      throw new InvalidTurnException(turn);
    }
    WorldAndHistory wh = _data.get(turn).get(worldId);
    return (wh == null) ? null : wh.getHistory();
  }

  /**
   * Load history for a specific world
   */
  @Override
  public synchronized List<IHistory> loadHistory(WorldId worldId) {
    Objects.requireNonNull(worldId, "worldId May not be null");
    if(!cacheInitialised()) {
      initCache();
    }
    int maxTurn = getMapInfo().getMaxTurn();
    List<IHistory> list = new ArrayList<IHistory>(maxTurn);
    list.add(null); //There is no turn 0 and we want turn to be the index
    for(int turn = 1; turn <= maxTurn; turn++) {
      WorldAndHistory wh = _data.get(turn).get(worldId);
      IHistory history = (wh == null) ? null : wh.getHistory();
      list.add(history);
    }
    return list;
  }

  @Override
  public synchronized void saveHistories(Collection<IHistory> histories) {
    Objects.requireNonNull(histories, "histories may not be null");
    if(histories.isEmpty()) {
      return;
    }

    try(Connection connection = _dataSource.getConnection()) {
      for(IHistory h : histories) {
        saveHistoryInternal(connection, h);
      }
    } catch(Exception e) {
      throw new RuntimeException("Error saving histories", e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }
  
  @Override
  public synchronized Set<WorldAndHistory> getWorldHistory(int turn) {
    if(turn < 1) {
      throw new InvalidTurnException(turn);
    }
    if(!cacheInitialised()) {
      initCache();
    }
    if(turn >= _data.size()) {
      throw new InvalidTurnException(turn);
    }
    Set<WorldAndHistory> results = new HashSet<>(_data.get(turn).values());
    return Collections.unmodifiableSet(results);
  }

  /**
   * Load a world by name. This is case-insensitive. The name may not be null
   * but empty is allowed. (Note that in the case of multiple worlds in the db
   * sharing a name (something the game itself doesnt support but romeo does)
   * the result is undefined as to which will be returned. 
   */
  @Override
  public synchronized IWorld loadWorldByName(String name) {
    Objects.requireNonNull(name, "name may not be null");
    if(_data == null) {
      initCache();
    }
    return _worldsByName.get(name.toUpperCase(Locale.US).trim());
  }

  @Override
  public synchronized void dataChanged(EventObject event) {
    if(event instanceof SettingChangedEvent) {
      SettingChangedEvent sce = (SettingChangedEvent) event;      
      //If the default scanner range was changed we need to invalidate the cache as it was initialised
      //using that range for worlds without another range recorded
      if(ISettings.DEFAULT_SCANNER.equals(sce.getName())) { 
        flushCache();
      }
    } else {
      //A unit or world data was changed. These service notifications aren't granular as to details so
      //we anyhow just flush the entire cache of world data so it will get reloaded on next demand.
      //A high-performance application would really want this to be granular so we could just reload
      //or invalidate affected worlds, but for the single user desktop environment of romeo the
      //additional overhead won't even be noticed by the user.
      flushCache();
      //What might have made the above comment useful was if it stated why we need to flush on a change of units...
      //(assuming its because the scanner range of worlds could change and we put the default range into
      // worlds we have read? - but do we still do that?)
    }
  }

  /**
   * Returns true if there is data in the worlds_history table for this turn.
   * @param turn
   */
  @Override
  public boolean haveData(int turn) {
    if(turn < 1) {
      throw new InvalidTurnException(turn);
    }
    try(Connection connection = _dataSource.getConnection()) {
      final String sql = "SELECT COUNT(*) FROM WORLDS_HISTORY WHERE turn=?";
      Number rowCount = (Number) DbUtils.readSingleValue(sql, new Object[] { turn }, connection);
      return rowCount.longValue() > 0;
    } catch(Exception e) {
      throw new RuntimeException("Error checking is have data for turn " + turn, e);
    }
  }
  
  @Override
  public synchronized List<Map<String, Object>> getWorldsSummary(int turn) {
    if(turn<1) {
      throw new InvalidTurnException(turn);
    }
    final String[] columns = new String[] {
        "id", "name", "owner", "worldEi", "worldRer", "firepower", "labour", "capital","scanner"};
    int visualRange = (int)_settingsService.getLong(ISettings.DEFAULT_SCANNER);
    List<Map<String, Object>> results = new ArrayList<>();
    try(Connection connection = _dataSource.getConnection()) {
      String sql = "SELECT w.id AS id, w.name AS name, COALESCE(owner,'') AS owner, w.worldEi AS worldEi, w.worldRer AS worldRer, "
          + "COALESCE(firepower,0) AS firepower, COALESCE(labour,0) AS labour, COALESCE(capital,0) AS capital,"
          + " COALESCE(u.scanner,?) AS scanner"
          + " FROM worlds w LEFT JOIN"
          + " (SELECT worldId,owner, firepower,labour , capital FROM worlds_history WHERE turn=?) wh"
          + " ON w.id=wh.worldId"
          + " LEFT JOIN units u ON u.id=w.scannerId";
      ResultSet rs = DbUtils.readQuery(sql, new Object[] { visualRange, turn }, connection);
      while(rs.next()) {
        Map<String, Object> world = new TreeMap<String, Object>();
        DbUtils.readColumns(rs, world, columns);
        world.put("id", new WorldId((String)world.get("id"))); 
        results.add(world);
      }
      return results;
    } catch(Exception e) {
      throw new RuntimeException("Error performing worlds summary query ", e);
    }
  }
  
  //.................
  
  /**
   * Returns true if the cache is not in the flushed and empty state
   * @return cache ready
   */
  private boolean cacheInitialised() {
    return _data != null;
  }

  /**
   * Clear cached information
   */
  protected void flushCache() {
    _mapInfo = null;
    _data = null; //A null reference here indicates the cache is unloaded
    _worldsById = null;
    _worldsByName = null;
  }
  
  /**
   * Loads _all_ the world and history data from the database into memory. This
   * will be called by service methods that need the data and find the _data
   * reference to be null. (Any existing cache is implicitly flushed by this
   * method)
   */
  protected void initCache() {
    /*
     * The data is cached in a List of Maps. Each Map holds data for a specific
     * turn and is keyed by the worlds id. The values in the map are instances
     * of WorldHistoryStruct, which simply duct tapes together a World,History,
     * and Color under the one reference.
     */
    Log log = LogFactory.getLog(this.getClass());
    if(log.isDebugEnabled()) {
      log.trace("Loading world data into cache");
    }
    long startTime = System.currentTimeMillis();
    int defaultScannerRange = (int) _settingsService.getLong(ISettings.DEFAULT_SCANNER);
    try(Connection connection = _dataSource.getConnection()) {
      if(_mapInfo == null) {
        _mapInfo = getMapInfo(connection);
      }
      int historySize = _mapInfo.getMaxTurn() + 1;
      _data = new ArrayList<Map<WorldId, WorldAndHistory>>(historySize);
      _worldsById = new HashMap<WorldId, IWorld>();
      _worldsByName = new TreeMap<String, IWorld>(); //TreeMap to preserve alphabetical ordering
      for(int i = 0; i < historySize; i++) {
        _data.add(new HashMap<WorldId, WorldAndHistory>());
      }

      /*
       * We want a table that returns all the world and history and player color
       * data for all worlds and all turns. This needs to be ordered so as to
       * group together worlds as we expect to find all data related to a
       * specific world together before we move onto the next world when we are
       * reading the data into our in memory data structures.
       */
      final String sql = "SELECT" + " W.id,W.name,W.worldX,W.worldY,W.scannerId,W.notes,W.worldEi,W.worldRER,"
          + " H.worldId,H.turn,H.owner,H.firepower,H.labour,H.capital," + " P.color,P.team," + "U.scanner"
          + " FROM WORLDS W LEFT JOIN WORLDS_HISTORY H" + " ON W.id=H.worldID" + " LEFT JOIN PLAYERS P"
          + " ON UCASE(H.owner)=UCASE(P.name)" + " LEFT JOIN UNITS U" + " ON W.scannerId=U.id"
          + " ORDER BY W.name ASC, H.turn ASC;";
      ResultSet rs = DbUtils.readQuery(sql, null, connection);
      IWorld currentWorld = null;
      while(rs.next()) {
        WorldId worldId = new WorldId( rs.getString("id") );
        if(currentWorld == null || !currentWorld.getId().equals(worldId)) { 
          //We have reached a new world in the results so we instantiate a new object to hold the values
          String name = rs.getString("name");
          int worldX = rs.getInt("worldX");
          int worldY = rs.getInt("worldY");
          String scanner = rs.getString("scannerId");
          UnitId scannerId = (scanner==null||scanner.isEmpty()) ? null : new UnitId(scanner);
          String notes = rs.getString("notes");
          int worldEi = rs.getInt("worldEi");
          int worldRer = rs.getInt("worldRer");
          IWorld world = new WorldImpl(worldId, name, worldX, worldY, scannerId, notes, worldEi, worldRer);
          currentWorld = world;
          _worldsById.put(worldId, world);
          _worldsByName.put(name.toUpperCase(Locale.US), world); //doesn't actually bother with using nameLookup
          if(log.isTraceEnabled()) {
            log.trace("Loaded world:" + world);
          }
        } else { //This row refers to the same world as the previous one so refer to the same object
          ; // currentWorld remains unchanged from the previous row
        }
        int turn = rs.getInt("turn");
        //Read the world's history data for the turn to which this row relates
        //nb: due to the join there may be null columns here when there is no history for the turn 
        String owner = rs.getString("owner");
        double firepower = rs.getDouble("firepower");
        int labour = rs.getInt("labour");
        int capital = rs.getInt("capital");
        IHistory history = new HistoryImpl(
            worldId, 
            turn, 
            (owner==null) ? "" : owner,
            firepower, 
            labour, 
            capital);
        Color color = Convert.toColor(rs.getString("color"));
        if(color == null) {
          color = IPlayer.NOBODY.equals(history.getOwner()) 
              ? PlayerUtils.NOBODY_COLOR
              : PlayerUtils.SOMEBODY_COLOR;
        }
        String team = rs.getString("team");
        if(team==null) { team = ""; }
        //TODO - we need to revise how we deal with the no-scanner situation.
        //We can envisage changes to the game that allow for suppressing a scanner range to zero while the
        //visual range remains 25 for example. For now this isnt the case however.
        int scannerRange = rs.getInt("scanner");
        if(scannerRange == 0) {
          scannerRange = defaultScannerRange;
        }

        //Store the initialised WorldHistoryStruct in our cache
        WorldAndHistory worldAndHistory = new WorldAndHistory(currentWorld, history, color, scannerRange, team);
        _data.get(turn).put(worldId, worldAndHistory);
        if(log.isTraceEnabled()) {
          log.trace("Loaded history:" + history);
        }
      }
    } catch(Exception e) {
      throw new RuntimeException("Error loading world and history data", e);
    }
    long endTime = System.currentTimeMillis();
    log.debug("initialised world service cache in " + (endTime-startTime) + " milliseconds");
  }
  
  private void saveHistoryInternal(Connection connection, IHistory history) {
    Objects.requireNonNull(connection, "connection may not be null");
    Objects.requireNonNull(history, "history may not be null");
    Objects.requireNonNull(history.getWorldId(),"history.worldId may not be null here"); //can't save without the worldId
    if(history.getWorldId() == null) {
      throw new NullPointerException("worldId may not be null in history object");
    }
    final String sql = "MERGE INTO WORLDS_HISTORY H USING (VALUES ?,?,?,?,?,?)"
        + "  V (worldId,turn,owner,firepower,labour,capital)" + " ON (H.worldId=V.worldId AND H.turn=V.turn)"
        + " WHEN MATCHED THEN UPDATE SET H.worldId=V.worldId, H.turn=V.turn, H.owner=V.owner,"
        + "H.firepower=V.firepower, H.labour=V.labour, H.capital=V.capital"
        + " WHEN NOT MATCHED THEN INSERT (worldId,turn,owner,firepower,labour,capital)"
        + "  VALUES (V.worldId,V.turn,V.owner,V.firepower,V.labour,V.capital);";
    final Object[] parameters = new Object[] { history.getWorldId(), history.getTurn(), history.getOwner(),
        history.getFirepower(), history.getLabour(), history.getCapital() };
    try {
      DbUtils.writeQuery(sql, parameters, connection);
    } catch(Exception e) {
      throw new RuntimeException("Failed to save History " + history, e);
    }
  }
  
  /**
   * Executes SQL to save or update a world row in the database. Caller is
   * responsible for providing connection and exception handling and flushing
   * the cache first. Note that with the use of the IWorld interface to limit
   * writeability to world objects, this method no longer updates the id
   * property of the world passed in when saving new worlds.
   * Does not check for duplicates - that is caller's responsibility.
   * @param world
   * @return worldId
   */
  private WorldId saveWorldInternal(Connection connection, IWorld world) {
    Objects.requireNonNull(connection, "connection may not be null");
    Objects.requireNonNull(world, "world may not be null");
    if(world.getName().isEmpty()){
      throw new IllegalArgumentException("world.name may not be empty");
    }
    if(world.isNew()) {
      final WorldId id = new WorldId( _keyGen.createIdKey() );
      final String sql = "INSERT INTO WORLDS (id,name,worldX,worldY,scannerId,notes,worldEi,worldRer)"
          + " VALUES (?,?,?,?,?,?,?,?);";
      final Object[] parameters_insert = new Object[] { id, world.getName(), world.getWorldX(), world.getWorldY(),
          world.getScannerId(), world.getNotes(), world.getWorldEi(), world.getWorldRer() };
      DbUtils.writeQuery(sql, parameters_insert, connection);
      return id;
    } else {
      final Object[] parameters_update = new Object[] { world.getName(), world.getWorldX(), world.getWorldY(),
          world.getScannerId(), world.getNotes(), world.getWorldEi(), world.getWorldRer(), world.getId(), };
      final String sql = "UPDATE WORLDS SET " + "name=?," + "worldX=?," + "worldY=?," + "scannerId=?," + "notes=?,"
          + "worldEi=?," + "worldRer=?" + " WHERE id=?;";
      DbUtils.writeQuery(sql, parameters_update, connection);
      return world.getId();
    }
  }
  
  /**
   * Check the cache and the list of additionalNames to ensure that the name is
   * unique. If it isn't throw a {@link DuplicateRecordException} to allow for a
   * friendlier UI message. Note that the db has its own constraints set to
   * prevent saving such a record, but the display of the resulting exception is
   * not user friendly. 
   * @param world
   * @throws DuplicateRecordException
   */
  private void checkDuplicate(IWorld world) throws DuplicateRecordException {
    //Worlds may not have a name that is a duplicate of an existing name (case-insensitive) of another world
    //Case-insensitive duplicate name check performed via the cache.
    IWorld other = loadWorldByName( world.getName() ); //nb: will uppercasify it inside the method
    if(other != null && !other.getId().equals(world.getId())) {
      throw new DuplicateRecordException("World name may not be the same as that of an existing world: " + world.getName());
    }
  }
  
  /**
   * Read {@link MapInfo} from the database. Note that this method doesnt cache the
   * result.
   * @param connection
   * @return mapInfo
   * @throws Exception
   */
  private MapInfo getMapInfo(Connection connection) throws Exception {
    Objects.requireNonNull(connection, "connection may not be null");
    int maxTurn = (Integer)DbUtils.readSingleValue("SELECT COALESCE(MAX(turn),0) AS maxTurn FROM WORLDS_HISTORY", null, connection);
    final String sql = "SELECT COUNT(*) AS worldCount, min(worldX) AS leftBorder, max(worldX) AS rightBorder,"
        + "min(worldY) AS topBorder, max(worldY) AS bottomBorder FROM WORLDS;";
    ResultSet rs = DbUtils.readQuery(sql, null, connection);
    rs.next();
    int worldCount = rs.getInt("worldCount");
    MapInfo mapInfo = null;
    if(worldCount > 0) {
      mapInfo = new MapInfo(
        rs.getInt("leftBorder"),
        rs.getInt("topBorder"),
        rs.getInt("rightBorder"),
        rs.getInt("bottomBorder"),
        maxTurn );
          
    } else {
      mapInfo = new MapInfo(0,0,0,0,maxTurn);
    }
    rs.close();
    rs.close();
    return mapInfo;
  }

}
