package romeo.players.ui;

import java.awt.Color;
import java.util.EventObject;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerId;
import romeo.players.impl.PlayerImpl;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.ui.ColorPicker;
import romeo.ui.forms.ColorButton;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.IFormLogic;
import romeo.ui.forms.RomeoForm;
import romeo.utils.Convert;
import romeo.worlds.api.HistorySummary;
import romeo.worlds.api.IWorldService;

/**
 * Logic for the form used to view and edit {@link Player} records.
 */
public class PlayerFormLogic implements IFormLogic, IServiceListener {
  private static ColorPicker _colorPicker;

  ////////////////////////////////////////////////////////////////////////////

  private RomeoForm _form;
  private IPlayer _player;
  private IPlayerService _playerService;
  private IWorldService _worldService;
  private ISettingsService _settingsService;

  public PlayerFormLogic(IPlayerService playerService, IWorldService worldService, ISettingsService settingsService) {
    _playerService = Objects.requireNonNull(playerService);
    _worldService = Objects.requireNonNull(worldService);
    _settingsService = Objects.requireNonNull(settingsService);
    _playerService.addListener(this);
    _worldService.addListener(this);
    _settingsService.addListener(this);
  }

  /**
   * No-op
   */
  @Override
  public void inputChanged() {
    ;
  }

  /**
   * Initialise the form
   * @param form
   * @param record
   *          an instance of {@link Player}
   */
  @Override
  public void bind(RomeoForm form, Object record) {
    _form = form;
    loadWith((IPlayer) record);
    final ColorButton colorField = (ColorButton) _form.getEntryFields().get("color");
    //We set a new picker so that dialog location can be managed seperately to other record types
    if(_colorPicker == null)
      _colorPicker = new ColorPicker();
    colorField.setColorPicker(_colorPicker);
    //..
  }

  /**
   * Cancel whatever changes the user was making in the fields and reload the
   * record from the service, or if it was a new record reset the form to the
   * default values for a new record.
   */
  @Override
  public void cancelChanges() {
    if(_player.isNew()) {
      _form.close();
    } else { //Otherwise reload the old player record from the service
      loadWith(_playerService.loadPlayer(_player.getId()));
    }
  }

  /**
   * Delete the player record using the service
   */
  @Override
  public void deleteRecord() {
    if(!_player.isNew()) {
      _playerService.deletePlayer(_player.getId());
    } else {
      cancelChanges();
    }
  }

  /**
   * Load form fields from the specified player information.
   * @param player
   */
  public void loadWith(IPlayer player) {
    Objects.requireNonNull(player, "player must not be null");
    _player = player;
    if(_form != null) {
      String name = player.getName();
      ((JTextField) _form.getEntryFields().get("name")).setText(name);
      ((JTextField) _form.getEntryFields().get("status")).setText(player.getStatus());
      ((JTextArea) _form.getEntryFields().get("notes")).setText(player.getNotes());
      ((ColorButton) _form.getEntryFields().get("color")).setColor(player.getColor());
      ((JTextField) _form.getEntryFields().get("team")).setText(player.getTeam());
      _form.setDirty(false);
      _form.setNew(_player.getId() == null);
      if("".equals(name) || name == null) {
        _form.setName("Untitled Player");
      } else {
        _form.setName(_player.getId() == null ? "Create New Player" : "Player - " + name);
      }
      updateSummaryFields(); //Update the displayed summary information
      _form.dataChanged();
    }
  }

  /**
   * Update the {@link Player} bean and save the changes using the
   * {@link IPlayerService}.
   */
  @Override
  public void saveChanges() {
    String name = ((JTextField) _form.getEntryFields().get("name")).getText();
    String status = ((JTextField) _form.getEntryFields().get("status")).getText();
    String notes = ((JTextArea) _form.getEntryFields().get("notes")).getText();
    Color color = ((ColorButton) _form.getEntryFields().get("color")).getColor();
    String team = ((JTextField) _form.getEntryFields().get("team")).getText();
    
    PlayerImpl newPlayer = new PlayerImpl(_player.getId(), name, status, notes, color, team);
    PlayerId id = _playerService.savePlayer(newPlayer);
    _player = new PlayerImpl(id, newPlayer);

    //Now call set player to ensure we have latest info (in case service changed anything like id for example)
    loadWith(_player);
  }

  /**
   * Callback handler for the service notification.
   * @param service
   */
  @Override
  public void dataChanged(EventObject event) {
    if(_player == null) {
      return;
    } //Not bound yet
    if(event instanceof SettingChangedEvent) {
      SettingChangedEvent sce = (SettingChangedEvent) event;
      if(ISettings.CURRENT_TURN.equals(sce.getName())) {
        updateSummaryFields();
      }
    } else if(event.getSource() instanceof IPlayerService) {
      if(_player.getId() != null && !_form.isDirty()) { //Only load fresh data if user hadnt started an edit operation
        IPlayerService service = (IPlayerService) event.getSource();
        IPlayer reloaded = service.loadPlayer(_player.getId());
        if(reloaded == null) { //It was deleted
          loadWith(new PlayerImpl());
          _form.setDirty(true);
        } else { //Load fresh data into the form
          loadWith(reloaded);
        }
      }
    } else if(event.getSource() instanceof IWorldService) {
      updateSummaryFields();
    }
  }

  protected void updateSummaryFields() {
    JLabel turnField = (JLabel) _form.getEntryFields().get("turn");
    JLabel totalFirepowerField = (JLabel) _form.getEntryFields().get("totalFirepower");
    JLabel worldCountField = (JLabel) _form.getEntryFields().get("worldCount");
    JLabel totalLabourField = (JLabel) _form.getEntryFields().get("totalLabour");
    JLabel totalCapitalField = (JLabel) _form.getEntryFields().get("totalCapital");

    if(_player.getId() == null) {
      turnField.setText("n/a");
      totalFirepowerField.setText("n/a");
      worldCountField.setText("n/a");
      totalLabourField.setText("n/a");
      totalCapitalField.setText("n/a");
    } else {
      String owner = _player.getName();
      int turn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);
      HistorySummary summary = _worldService.getSummary(owner, turn);

      turnField.setText(turn + "");
      totalFirepowerField.setText(Convert.toStr(summary.getTotalFirepower(), 2));
      worldCountField.setText(summary.getWorldCount() + "");
      totalLabourField.setText(summary.getTotalLabour() + " \u03c1");
      totalCapitalField.setText(summary.getTotalCapital() + " \u03c5");
    }
  }

  /**
   * Will remove the form as a listener from the services to facilitate gc
   */
  @Override
  public void dispose() {
    _playerService.removeListener(this);
    _worldService.removeListener(this);
    _settingsService.removeListener(this);
  }

  /**
   * This form does not define any custom fields.
   * @return null
   */
  @Override
  public JComponent initCustom(RomeoForm form, FieldDef field) {
    return null;
  }

}
