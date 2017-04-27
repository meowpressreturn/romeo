package romeo.settings.api;

/**
 * Defines constants for various setting names
 */
public interface ISettings {
  public static final String IMPORT_FOLDER = "romeo.files.importFolder";
  public static final String CURRENT_TURN = "romeo.game.defaultGame.currentTurn";
  
  /**
   * Default scanning range for worlds that do not have a scanner unit
   */
  public static final String DEFAULT_SCANNER = "romeo.worlds.defaultScanner";
  
  public static final String NUMBER_OF_BATTLES = "romeo.simulator.numberOfBattles";
  public static final String SHOW_RAW_FP = "romeo.simulator.showRawFp";
  public static final String F_CLASS_WORLD = "romeo.worlds.sizes.fClass";
  public static final String E_CLASS_WORLD = "romeo.worlds.sizes.eClass";
  public static final String D_CLASS_WORLD = "romeo.worlds.sizes.dClass";
  public static final String C_CLASS_WORLD = "romeo.worlds.sizes.cClass";
  public static final String B_CLASS_WORLD = "romeo.worlds.sizes.bClass";
  public static final String A_CLASS_WORLD = "romeo.worlds.sizes.aClass";
  public static final String DEFCON_5 = "romeo.worlds.defcon.5";
  public static final String DEFCON_4 = "romeo.worlds.defcon.4";
  public static final String DEFCON_3 = "romeo.worlds.defcon.3";
  public static final String DEFCON_2 = "romeo.worlds.defcon.2";
  public static final String MAP_SPEED = "romeo.worlds.map.speed";
  public static final String MAP_ZOOM = "romeo.worlds.map.zoom";
  public static final String MAP_ORIGIN = "romeo.worlds.map.origin";
  public static final String MAP_SHOW_NAME = "romeo.worlds.map.showName";
  public static final String MAP_SHOW_SCANNER = "romeo.worlds.map.showScanner";
  public static final String MAP_SHOW_RANGE = "romeo.worlds.map.showRange";
  public static final String MAP_SHOW_FIREPOWER = "romeo.worlds.map.showFirepower";
  public static final String MAP_SHOW_DELTAS = "romeo.worlds.map.showDeltas";
  public static final String MAP_SHOW_LABOUR = "romeo.worlds.map.showLabour";
  public static final String MAP_SHOW_CAPITAL = "romeo.worlds.map.showCapital";
  public static final String MAP_SHOW_OWNER = "romeo.worlds.map.showOwner";
  public static final String WINDOW_WIDTH = "romeo.ui.window.width";
  public static final String WINDOW_HEIGHT = "romeo.ui.window.height";
  public static final String LEFT_PANE_WIDTH = "romeo.ui.leftPaneWidth";
  public static final String MAP_X = "romeo.worlds.map.x";
  public static final String MAP_Y = "romeo.worlds.map.y";
  public static final String SELECTED_TAB = "romeo.ui.selectedTab";
  public static final String SELECTED_DATA_TAB = "romeo.ui.selectedDataTab";
}
