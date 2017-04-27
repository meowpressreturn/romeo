package romeo.importdata.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.importdata.IWorldFile;
import romeo.importdata.IWorldImportReport;
import romeo.importdata.IWorldImporter;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerUtils;
import romeo.players.impl.PlayerImpl;
import romeo.settings.api.ISettingsService;
import romeo.units.api.UnitId;
import romeo.utils.Convert;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldId;
import romeo.worlds.impl.HistoryImpl;
import romeo.worlds.impl.WorldImpl;

public class WorldImporterImpl implements IWorldImporter {

  //References to other objects we need to use
  protected final IWorldService _worldService;
  protected final IPlayerService _playerService;
  protected final ISettingsService _settingsService;
  private final Log _log = LogFactory.getLog(WorldImporterImpl.class); 

  //State (used during import process)
  protected Map<String, IPlayer> _players; //Used to lookup players we already imported.
  private WorldImportReportImpl _report;

  /**
   * Constructor. Service references are required.
   * @param turn
   *          The turn for which data is to be added to history
   * @param worldService
   * @param playerService
   * @param settingsService
   * @param ignoreNullsFor
   */
  public WorldImporterImpl(IWorldService worldService,
                           IPlayerService playerService,
                           ISettingsService settingsService) {
    _worldService = Objects.requireNonNull(worldService, "worldService must not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService must not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
  }

  /**
   * Imports map data from the csv into the db (accessed via the worldFile
   * object) according to the settings configured for this importer instance.
   * @param worldFile
   * @return report
   */
  @Override
  public IWorldImportReport importData(IWorldFile worldFile, int turn) {
    Objects.requireNonNull(worldFile, "worldFile must not be null");
    if(turn < 1) {
      throw new IllegalArgumentException("Invalid turn:" + turn);
    }
    _report = new WorldImportReportImpl();
    List<IWorld> worlds = new ArrayList<IWorld>();
    _players = new TreeMap<String, IPlayer>();
    for(IPlayer player : _playerService.getPlayers()) { //Init lookup table with players we already have in database
      _players.put(player.getName().toUpperCase(Locale.US), player);
    }
    List<IPlayer> importPlayers = new ArrayList<>();
    List<IHistory> histories = new ArrayList<>();
    try {
      String nameColumn = worldFile.getNameColumn();
      Iterator<Map<String, String>> i = worldFile.iterator();
      while(i.hasNext()) {
        Map<String, String> worldData = i.next();
        if(_log.isDebugEnabled()) {
          _log.debug("Analysing data:" + worldData);
        }

        //Check to see if we have a record for this player already
        String owner = worldData.get("owner");
        IPlayer player = _players.get(owner.toUpperCase(Locale.US));
        if(player == null) {
          //Create a new record for the playerif we don't already have one.
          //Note that there is no update of existing players done here.
          String team = worldData.get("team");
          player = importPlayer(owner, team);
          importPlayers.add(player);
        }

        String name = (String)worldData.get(nameColumn);
        
        try {
          IWorld world = null;
          IWorld savedWorld = _worldService.getWorldByName(name);
          if(savedWorld == null) { //IMPORT WORLD WE DONT HAVE YET
            world = createWorld(worldData,null,null); //Get data from the CSV as a World object
            _report.addImportedWorld();
          } else { //UPDATE WORLD THAT IS ALREADY IN DATABASE, preserving id and scanner from the saved world record
            world = createWorld(worldData,savedWorld.getId(), savedWorld.getScannerId());
            _report.addUpdatedWorld();
          }
          worlds.add(world); //Created and updated both go in this list. Order is vital for later history linking.

          double firepower = Convert.toDouble(worldData.get("firepower"));
          int labour = Convert.toInt(worldData.get("labour"));
          int capital = Convert.toInt(worldData.get("capital"));
          //Store a temporary object in the histories table. Later we will retrieve it and create a new one to actually
          //save that includes the worldId. Right now, only pre-existing worlds have an id.
          IHistory history = new HistoryImpl(null, turn, owner, firepower, labour, capital);
          histories.add(history); //Order is vital for later linking to world by id.
        } catch(Exception e) {
          throw new RuntimeException("Error interpreting data for " + name, e);
        }
      }
      //Save imported players
      _playerService.savePlayers(importPlayers);

      //Save the new or changed worlds
      List<WorldId> worldIds = _worldService.saveWorlds(worlds);

      //Link worlds to histories
      List<IHistory> toSave = new ArrayList<IHistory>(histories.size());
      for(int idIndex=0; idIndex < worldIds.size(); idIndex++) {
        WorldId id = worldIds.get(idIndex);
        IHistory history = histories.get(idIndex);
        history = new HistoryImpl(id, history);
        toSave.add(history);
      }
      _worldService.saveHistories(toSave);
      _log.info("Changes saved");
    } catch(Exception e) {
      Exception ex = new RuntimeException("Import Failure:" + e.getMessage(), e);
      _report.setException(ex);
    }
    return _report;
  }

  /**
   * Create a new {@link Player} record using the specified name, and add it to
   * the lookup table and the list of players to be saved to the database.
   * The new record hasn't been added to the db yet, so will have a null id.
   * @param name
   * @param team
   */
  protected IPlayer importPlayer(String name, String team) {
    name = (name==null) ? "" : name.trim();
    team = (team==null) ? "" : team.trim();
    Color color = null;
    if(team.isEmpty()) {        
      color = IPlayer.NOBODY.equalsIgnoreCase(name) ? PlayerUtils.NOBODY_COLOR : PlayerUtils.SOMEBODY_COLOR;
    } else {
      color = PlayerUtils.getTeamColor(team);
    }
    IPlayer player = new PlayerImpl(null, name, "", "", color, team);
    _players.put(name.toUpperCase(Locale.US), player);
    return player;
  }

  /**
   * Create a world record using values from the map
   * @param worldData
   * @param world
   */
  protected IWorld createWorld(Map<String, String> data, WorldId worldId, UnitId scannerId) {
    String name = data.get("name");
    int worldX = Convert.toInt( data.get("worldX") );
    int worldY = Convert.toInt( data.get("worldY") );
    String notes = ""; //not in csv
    int worldEi = Convert.toInt( data.get("worldEi") );
    int worldRer = Convert.toInt( data.get("worldRer") );    
    WorldImpl world = new WorldImpl(worldId, name, worldX, worldY, scannerId, notes, worldEi, worldRer);
    return world;
  }
}
