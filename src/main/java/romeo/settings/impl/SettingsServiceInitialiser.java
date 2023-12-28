package romeo.settings.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceInitialiser;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService.SettingType;
import romeo.ui.DataTabs;
import romeo.ui.MainFrame;
import romeo.utils.DbUtils;

/**
 * Prepares the various SETTINGS_xxx tables used to persist various simple settings
 */
public class SettingsServiceInitialiser implements IServiceInitialiser {
  
  /**
   * Quick & dirty way of passing the path from which the unit file was copied (if the dialog was used on initial
   * startup) to the settings service initialiser to use instead of the user directory as the default import folder
   * setting. In 0.62 this defaulted to an empty string and in 0.6.3 if the unit file is still pre-placed in the
   * resources folder it will continue to be the case. Otherwise Romeo class will set this to the folder containing
   * the unit file the user selected in the dialog. 
   */
  public static String __initialImportFolderPath = "";
  
  ////////////////////////////////////////////////////////////////////////////

  @Override
  public void init(Set<String> tableNames, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    
    //Note: the value columns are nullable, but the names certainly are not
    
    if(!tableNames.contains("SETTINGS_LONG")) {
      String sql = "CREATE TABLE SETTINGS_LONG (" + "name VARCHAR DEFAULT '' NOT NULL PRIMARY KEY" + ",value BIGINT" + ");";
      log.info("Creating SETTINGS_LONG table");
      DbUtils.writeQuery(sql, null, connection);
    }

    if(!tableNames.contains("SETTINGS_STRING")) {
      String sql = "CREATE TABLE SETTINGS_STRING (" + "name VARCHAR DEFAULT '' NOT NULL PRIMARY KEY" + ",value VARCHAR" + ");";
      log.info("Creating SETTINGS_STRING table");
      DbUtils.writeQuery(sql, null, connection);
    }

    if(!tableNames.contains("SETTINGS_DOUBLE")) {
      String sql = "CREATE TABLE SETTINGS_DOUBLE (" + "name VARCHAR DEFAULT '' NOT NULL PRIMARY KEY" + ",value DOUBLE" + ");";
      log.info("Creating SETTINGS_DOUBLE table");
      DbUtils.writeQuery(sql, null, connection);
    }

    if(!tableNames.contains("SETTINGS_FLAG")) {
      String sql = "CREATE TABLE SETTINGS_FLAG (" + "name VARCHAR DEFAULT '' NOT NULL PRIMARY KEY" + ",value BOOLEAN" + ");";
      log.info("Creating SETTINGS_FLAG table");
      DbUtils.writeQuery(sql, null, connection);
    }

    initSettings(connection);
  }

  protected void initSettings(Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    log.info("Preparing initial settings values");
    initSetting(connection, ISettings.WINDOW_WIDTH, SettingType.LONG, 1024, false);
    initSetting(connection, ISettings.WINDOW_HEIGHT, SettingType.LONG, 650, false);
    initSetting(connection, ISettings.LEFT_PANE_WIDTH, SettingType.LONG, 527, false); //was 435
    initSetting(connection, ISettings.MAP_X, SettingType.LONG, 0, false);
    initSetting(connection, ISettings.MAP_Y, SettingType.LONG, 0, false);
    initSetting(connection, ISettings.DEFAULT_SCANNER, SettingType.LONG, 25, false);
    initSetting(connection, ISettings.CURRENT_TURN, SettingType.LONG, 1, false);
    initSetting(connection, ISettings.NUMBER_OF_BATTLES, SettingType.LONG, 1000, false);
    initSetting(connection, ISettings.SHOW_RAW_FP, SettingType.FLAG, false, false);
    
    initSetting(connection, ISettings.IMPORT_FOLDER, SettingType.STRING, __initialImportFolderPath, false);

    initSetting(connection, ISettings.F_CLASS_WORLD, SettingType.LONG, 100, false);
    initSetting(connection, ISettings.E_CLASS_WORLD, SettingType.LONG, 300, false);
    initSetting(connection, ISettings.D_CLASS_WORLD, SettingType.LONG, 1000, false);
    initSetting(connection, ISettings.C_CLASS_WORLD, SettingType.LONG, 3000, false);
    initSetting(connection, ISettings.B_CLASS_WORLD, SettingType.LONG, 8000, false);
    initSetting(connection, ISettings.A_CLASS_WORLD, SettingType.LONG, 15000, false);

    initSetting(connection, ISettings.DEFCON_5, SettingType.LONG, 20, false);
    initSetting(connection, ISettings.DEFCON_4, SettingType.LONG, 60, false);
    initSetting(connection, ISettings.DEFCON_3, SettingType.LONG, 200, false);
    initSetting(connection, ISettings.DEFCON_2, SettingType.LONG, 1000, false);

    initSetting(connection, ISettings.MAP_SHOW_NAME, SettingType.FLAG, true, false);
    initSetting(connection, ISettings.MAP_SHOW_SCANNER, SettingType.FLAG, false, false);
    initSetting(connection, ISettings.MAP_SHOW_RANGE, SettingType.FLAG, true, false);
    initSetting(connection, ISettings.MAP_SHOW_FIREPOWER, SettingType.FLAG, true, false);
    initSetting(connection, ISettings.MAP_SHOW_DELTAS, SettingType.FLAG, false, false);
    initSetting(connection, ISettings.MAP_SHOW_LABOUR, SettingType.FLAG, false, false);
    initSetting(connection, ISettings.MAP_SHOW_CAPITAL, SettingType.FLAG, false, false);
    initSetting(connection, ISettings.MAP_SHOW_OWNER, SettingType.FLAG, false, false);
    initSetting(connection, ISettings.MAP_SPEED, SettingType.LONG, 0, false);
    initSetting(connection, ISettings.MAP_ZOOM, SettingType.DOUBLE, 1.5d, false);
    initSetting(connection, ISettings.MAP_ORIGIN, SettingType.STRING, "", false);
    initSetting(connection, ISettings.SELECTED_TAB, SettingType.STRING, MainFrame.TAB_NAME_SIMULATOR, false);
    initSetting(connection, ISettings.SELECTED_DATA_TAB, SettingType.STRING, DataTabs.TAB_NAME_PLAYERS, false);
  }

  /**
   * Internal method to create the row for the setting in the specified table.
   * If the update parameter is passed as true then it will also update the
   * value of the setting if it already exists in the database. Warning: the
   * table name is used directly in sql without sanitation or verification
   * @param connection
   * @param name
   *          name of the setting
   * @param table
   *          table to write the value to
   * @param value
   *          the initial value for this setting
   * @param update
   *          change the value even if it already exists in db
   */
  protected void initSetting(Connection connection, String name, SettingType type, Object value, boolean update) {

    String table = SettingsServiceImpl.tableName(type);
    final String sql = "MERGE INTO " + table + " S USING (VALUES ?,?) V (name, value)" + " ON (S.name = V.name)"
        + (update ? " WHEN MATCHED THEN UPDATE SET S.name = V.name, S.value=V.value" : "")
        + " WHEN NOT MATCHED THEN INSERT (name,value) VALUES (V.name, V.value);";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1,name);
      ps.setObject(2,value);
      ps.execute();
      int count = ps.getUpdateCount();
      if(count > 0) {
        LogFactory.getLog(this.getClass()).info("Initialised setting " + name + " with value " + value);
      } else {
        LogFactory.getLog(this.getClass()).debug("Setting " + name + " exists and was not modified" );
      }
    } catch(SQLException e) {
      throw new RuntimeException("Error initialising setting " + name,e);
    }
  }

  /**
   * Clears out only those settings that are game-specific. Many settings are
   * not specific to a particular game and are thus left untouched by this call.
   * @param connection
   */
  @Override
  public void reset(Connection connection) {
    ;

    //todo - suggest use namesace for game specific setting in anticipation
    //       of multigame support. eg "game.defaultGame.currentTick"
    //       Then we can do things like just delete all those with the game.defaultGame prefix
    //       to clear the game specific settings for the game 'defaultGame' etc
  }

}
