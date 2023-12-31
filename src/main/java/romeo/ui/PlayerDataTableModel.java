package romeo.ui;

import java.util.Collections;
import java.util.EventObject;
import java.util.Objects;

import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayerService;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.worlds.api.IWorldService;

/**
 * Specialised table model for the players data table. In addition to the
 * {@link IPlayerService} it also needs to listen to the {@link IWorldService}
 * for changes to world history that affect the summary information (eg: visible
 * firepower) and the {@link ISettingsService} for changes to the currently
 * selected turn.
 */
public class PlayerDataTableModel extends BeanTableModel implements IServiceListener {

  private static final ColumnDef[] columns = new BeanTableModel.ColumnDef[] {
      new BeanTableModel.ColumnDef("name", "Name"), new BeanTableModel.ColumnDef("team", "Team"),
      new BeanTableModel.ColumnDef("worlds", "Worlds"), new BeanTableModel.ColumnDef("visibleFp", "Visible FP"),
      new BeanTableModel.ColumnDef("labour", "Labour"), new BeanTableModel.ColumnDef("capital", "Capital"),
      new BeanTableModel.ColumnDef("status", "Status"), new BeanTableModel.ColumnDef("color", "Colour"), };

  ////////////////////////////////////////////////////////////////////////////

  private IPlayerService _playerService;
  private IWorldService _worldService;
  private ISettingsService _settingsService;

  public PlayerDataTableModel(IPlayerService playerService,
                              IWorldService worldService,
                              ISettingsService settingsService) {
    super(columns, Collections.emptyList());
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingService may not be null");

    _playerService.addListener(this);
    _worldService.addListener(this);
    _settingsService.addListener(this);
    dataChanged(new EventObject(this));
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event instanceof SettingChangedEvent) {
      String settingName = ((SettingChangedEvent)event).getName();
      if(! (ISettings.DEFAULT_SCANNER.equals(settingName) || ISettings.CURRENT_TURN.equals(settingName) ) ) {
        return; //Ignore irrelevant setting changes
      }
    }
    int turn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);
    _data = _playerService.getPlayersSummary(turn);
    sortRows();
    fireTableDataChanged();
  }

}
