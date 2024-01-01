package romeo.ui;

import java.awt.Color;
import java.awt.Point;
import java.util.Objects;

import org.slf4j.Logger;

import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.api.WorldId;

/**
 * Invoked by Romeo to centre the map on the saved coordinates in settings after the MainFrame has been
 * displayed (and thus its scrollpane will return its correct size). This will also set the origin world.
 */
public class MapCenterer implements Runnable {
  
  private final Logger _log;
  private final ISettingsService _settingsService;
  private final IWorldService _worldService;
  private final GenericMap _worldsMap;
  
  public MapCenterer(
      final Logger log,
      final ISettingsService settingsService, 
      final IWorldService worldService, 
      final GenericMap worldsMap) {
    _log = Objects.requireNonNull(log, "log may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _worldsMap = Objects.requireNonNull(worldsMap, "worldsMap may not be null");
  }

  @Override
  public void run() {
    int x = (int)_settingsService.getLong(ISettings.MAP_X);
    int y = (int)_settingsService.getLong(ISettings.MAP_Y);
    
    _log.trace("Setting visibleMapCentre to " + x + "," + y);
    _worldsMap.setVisibleMapCentre( new Point(x,y) );
    
    String originString = _settingsService.getString(ISettings.MAP_ORIGIN);
    if(originString != null && !originString.isEmpty()) {
      WorldId originId = new WorldId(originString);
      IWorld originWorld = _worldService.getWorld(originId);
      if(originWorld != null) {
        int turn = (int)_settingsService.getLong(ISettings.CURRENT_TURN);
        IHistory originHistory = _worldService.getHistory(originId, turn);
        if(originHistory != null) {
          //the history and color information isnt actually used in this case,
          //but is needed to construct the object expected by the map
          //(we didnt really need to load an actual history either)
          WorldAndHistory originWah = new WorldAndHistory(originWorld, originHistory, Color.BLACK, 0, "");
          _log.trace("Setting origin to " + originWah);
          _worldsMap.setOrigin( originWah );
        } else {
          _log.debug("Unable to set origin because history not found for " + originWorld + " for turn: " + turn);
        }
      } else {
        _log.debug("Unable to set origin because world not found: " + originId);
      }
      
    }    
  }

}



















