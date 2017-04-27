package romeo.ui;

import java.util.Collections;
import java.util.EventObject;
import java.util.Objects;

import romeo.model.api.IServiceListener;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.worlds.api.IWorldService;

public class WorldDataTableModel  extends BeanTableModel implements IServiceListener {

  private static final ColumnDef[] columns = new BeanTableModel.ColumnDef[] {
      new BeanTableModel.ColumnDef("name", "Name"), 
      new BeanTableModel.ColumnDef("owner", "Owner"),
      new BeanTableModel.ColumnDef("scanner", "Scan"),
      new BeanTableModel.ColumnDef("firepower", "FP"), 
      new BeanTableModel.ColumnDef("worldEi", "EI"),
      new BeanTableModel.ColumnDef("worldRer", "RER"),
      new BeanTableModel.ColumnDef("labour", "Labour"), 
      new BeanTableModel.ColumnDef("capital", "Capital")
   };

  ////////////////////////////////////////////////////////////////////////////

  private IWorldService _worldService;
  private ISettingsService _settingsService;

  public WorldDataTableModel( IWorldService worldService,
                              ISettingsService settingsService) {
    super(columns, Collections.emptyList());
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingService may not be null");

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
    _data = _worldService.getWorldsSummary(turn);
    sortRows();
    fireTableDataChanged();
  }
}



















