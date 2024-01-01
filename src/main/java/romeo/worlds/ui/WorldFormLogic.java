package romeo.worlds.ui;

import java.awt.Dimension;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

import org.slf4j.LoggerFactory;

import romeo.Romeo;
import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.ui.PlayerFormFactory;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.ui.BeanTableHeaderRenderer;
import romeo.ui.BeanTableModel;
import romeo.ui.NavigatorPanel;
import romeo.ui.NumericCellRenderer;
import romeo.ui.TableNavigatorMediator;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.IFormLogic;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.ScannerCombo;
import romeo.units.api.IUnit;
import romeo.units.api.UnitId;
import romeo.utils.BeanComparator;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.api.WorldId;
import romeo.worlds.impl.HistoryImpl;
import romeo.worlds.impl.WorldImpl;

public class WorldFormLogic implements IFormLogic, IServiceListener {
  
  private WorldForm _form;
  private IWorld _world;
  private IHistory _history;
  
  private final IWorldService _worldService;
  private final ISettingsService _settingsService;
  private final IPlayerService _playerService;
  private final PlayerFormFactory _playerFormFactory;
  
  public WorldFormLogic(
      IWorldService worldService, 
      ISettingsService settingsService,
      IPlayerService playerService,
      PlayerFormFactory playerFormFactory) {
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _playerFormFactory = Objects.requireNonNull(playerFormFactory, "playerFormFactory may not be null");
  }

  @Override
  public void bind(RomeoForm form, Object record) {
    _form = (WorldForm)Objects.requireNonNull(form, "form may not be null");    
    Objects.requireNonNull(record, "record may not be null");    
    IWorld world = null;
    if(record instanceof WorldId) {
      world = _worldService.getWorld( (WorldId)record );
    }
    else if(record instanceof WorldAndHistory) {
      world = ((WorldAndHistory)record).getWorld();
    } else if(record instanceof IWorld) {
      world = (IWorld)record;
    } else {
      throw new ClassCastException("record is not a World or WorldAndHistory: " + record.getClass().getName());
    }
    loadWith(world);
    _worldService.addListener(this);
    _settingsService.addListener(this);
  }

  @Override
  public void cancelChanges() {
    if(_world.isNew()) {
      _form.close();
    } else {
      loadWith(_worldService.getWorld(_world.getId()));
    }
  }

  @Override
  public void deleteRecord() {
    if(!_world.isNew()) {
      _worldService.deleteWorld(_world.getId());
    } else {
      cancelChanges();
    }
  }

  /**
   * Detach listener references to allow this form instance to be garbage collected.
   */
  @Override
  public void dispose() {
    _worldService.removeListener(this);
    _settingsService.removeListener(this);
    TableNavigatorMediator tnm = _form.getHistoryTnm();
    if(tnm != null) {
      tnm.stopListening();
    }
  }

  @Override
  public void inputChanged() {
    ;
  }

  /**
   * Creates the history table.
   * @return scrollPane a scrollPane which the history table display inside
   */
  @Override
  public JComponent initCustom(RomeoForm form, FieldDef field) {
    if("history".equals(field.getName())) {
      List<IHistory> data = Collections.emptyList();
      BeanTableModel.ColumnDef[] columns = new BeanTableModel.ColumnDef[] {
          new BeanTableModel.ColumnDef("turn", "Turn"), 
          new BeanTableModel.ColumnDef("owner", "Owner"),
          new BeanTableModel.ColumnDef("firepower", "FP"),
          new BeanTableModel.ColumnDef("labour", "Labour"),
          new BeanTableModel.ColumnDef("capital", "Capital"),
      };
      BeanTableModel model = new BeanTableModel(columns, data);
      JTable table = new JTable(model);
      GuiUtils.setColumnWidths(table, new int[] { 24, 64, 32, 32, 32, });
      JScrollPane scrollPane = new JScrollPane(table);
      GuiUtils.initScrollIncrement(scrollPane);
      table.getTableHeader().setDefaultRenderer(
          new BeanTableHeaderRenderer((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()));
      table.setDefaultRenderer(Double.class, new NumericCellRenderer(2));
      scrollPane.setPreferredSize(new Dimension(1, 128));
      ((WorldForm)form).setHistoryTable(table);

      //Make it so clicking on a row in the table , opens the relevant player's form
      NavigatorPanel navigatorPanel = Romeo.getMainFrame().getNavigatorPanel();
      WorldFormHistoryTableListener listener = new WorldFormHistoryTableListener(
          LoggerFactory.getLogger(WorldFormHistoryTableListener.class),
          navigatorPanel, 
          _playerService, 
          _playerFormFactory);
      ((WorldForm)form).setHistoryTnm( new TableNavigatorMediator(table, listener) );

      return scrollPane;
    }
    return null;
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event.getSource() instanceof IWorldService) {
      if(_world.getId() != null && !_form.isDirty()) { //Only load fresh data if user hadnt started an edit operation
        IWorldService service = (IWorldService) event.getSource();
        IWorld reloaded = service.getWorld(_world.getId());
        if(reloaded == null) { //It was deleted (turn this into a new world form with the old values in case they want it back)
          loadWith( new WorldImpl(null,_world) );
          _form.setDirty(true);
        } else { //Load fresh data into the form
          loadWith(reloaded);
        }
      }
    }

    if(event.getSource() instanceof ISettingsService) {
      //TODO - only update on change of turn or default scanner range
      loadWith(_world);
    }
  }

  @Override
  public void saveChanges() {
    Map<String, JComponent> fields = _form.getEntryFields();
    String name = ((JTextField) fields.get("name")).getText().trim();
    String worldXStr = ((JTextField) fields.get("worldX")).getText();
    int worldX = Convert.toInt(worldXStr);
    String worldYStr = ((JTextField) fields.get("worldY")).getText();
    int worldY = Convert.toInt(worldYStr);
    IUnit scanner = ((ScannerCombo) fields.get("scanner")).getScanner();
    UnitId scannerId = (scanner == null) ? null : scanner.getId();
    String notes = ((JTextArea) fields.get("notes")).getText();
    String worldEiStr = ((JTextField) fields.get("worldEi")).getText();
    int worldEi = Convert.toInt(worldEiStr);
    String worldRerStr = ((JTextField) fields.get("worldRer")).getText();
    int worldRer = Convert.toInt(worldRerStr);
    
    WorldId worldId = _world.getId(); //Will be null for a new world
    IWorld newWorld = new WorldImpl(worldId, name, worldX, worldY, scannerId, notes, worldEi, worldRer);

    int turn = Convert.toInt(((JLabel) fields.get("turnLabel")).getText());
    String owner = ((JTextField) fields.get("owner")).getText().trim();
    double firepower = Convert.toDouble(((JTextField) fields.get("firepower")).getText());
    int labour = Convert.toInt(((JTextField) fields.get("labour")).getText());
    int capital = Convert.toInt(((JTextField) fields.get("capital")).getText());

    if(owner.isEmpty() && labour==0 && capital==0 && firepower==0) {
      //If no history was entered for the turn we don't save any. nb: an unowned world should really have
      //Nobody as owner.
      worldId = _worldService.saveWorld(newWorld);
      
    } else {
      IHistory history = new HistoryImpl(worldId, turn, owner, firepower, labour, capital);
      worldId = _worldService.saveWorldWithHistory(newWorld, history);
    }
    newWorld = new WorldImpl(worldId, newWorld);
    loadWith(newWorld);
  }
  
  /**
   * Update the form values from the specified World bean. Note that this will
   * also go and retrieve the History data for the world from the to populate
   * @param world
   */
  public void loadWith(IWorld world) {
    _world = world;
    String name = world.getName();
    Map<String, JComponent> fields = _form.getEntryFields();
    ((JTextField) fields.get("name")).setText(name);
    ((JTextField) fields.get("worldX")).setText("" + world.getWorldX());
    ((JTextField) fields.get("worldY")).setText("" + world.getWorldY());
    ((JTextField) fields.get("worldEi")).setText("" + world.getWorldEi());
    ((JTextField) fields.get("worldRer")).setText("" + world.getWorldRer());
    ((ScannerCombo) fields.get("scanner")).setScannerById(world.getScannerId());
    ((JTextArea) fields.get("notes")).setText(world.getNotes());

    _form.setNew(_world.getId() == null);
    if("".equals(name) || name == null) {
      _form.setName("Untitled World");
    } else {
      _form.setName(_world.getId() == null ? "Create New World" : "World - " + name);
    }

    int currentTurn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);
    //Load data for the history table
    List<IHistory> histories;
    if(world.isNew()) {
      histories = Collections.emptyList();
    } else {
      histories = _worldService.getHistory(world.getId());
    }
    BeanTableModel model = (BeanTableModel)_form.getHistoryTable().getModel();
    model.setData(histories);
    ListIterator<IHistory> i = histories.listIterator();
    _history = null;
    while(i.hasNext()) { //Find the current viewed turns info and clear out unknown (empty) rows
      IHistory history = i.next();
      if(history == null) {
        i.remove();
      } else if(currentTurn == history.getTurn()) {
        _history = history;
      }
    }
    Collections.sort(histories, new BeanComparator("turn", true, true));

    ((JLabel) fields.get("turnLabel")).setText("" + currentTurn); //nb: label value is used during saving
    if(_history != null) {
      
      ((JTextField) fields.get("owner")).setText(_history.getOwner());
      ((JTextField) fields.get("firepower")).setText("" + _history.getFirepower());
      ((JTextField) fields.get("labour")).setText("" + _history.getLabour());
      ((JTextField) fields.get("capital")).setText("" + _history.getCapital());
    } else {
      
      ((JTextField) fields.get("owner")).setText("");
      ((JTextField) fields.get("firepower")).setText("0");
      ((JTextField) fields.get("labour")).setText("0");
      ((JTextField) fields.get("capital")).setText("0");
      
      _history = new HistoryImpl(world.getId(), currentTurn, IPlayer.NOBODY, 0, 0, 0);
    }

    _form.setDirty(false);
    _form.dataChanged();
  }
}
