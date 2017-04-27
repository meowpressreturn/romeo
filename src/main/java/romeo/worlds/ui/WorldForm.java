package romeo.worlds.ui;

import java.awt.Dimension;
import java.util.ArrayList;
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

import romeo.Romeo;
import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
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

public class WorldForm extends RomeoForm implements IFormLogic, IServiceListener {
  protected IWorldService _worldService;
  protected IWorld _world;
  protected IHistory _history;
  protected JTable _historyTable;
  protected ISettingsService _settingsService;
  protected TableNavigatorMediator _tnm;

  public WorldForm(IWorldService worldService, ISettingsService settingsService) {
    Objects.requireNonNull(worldService, "worldService must not be null");
    Objects.requireNonNull(settingsService, "settingsService must not be null");
    setName("World");
    setForceTwoColumns(true);
    super.setFormLogic(this);
    _worldService = worldService;
    _settingsService = settingsService;
    ArrayList<FieldDef> fields = new ArrayList<FieldDef>();
    FieldDef nameDef = new FieldDef("name", "Name");
    nameDef.setMandatory(true);
    fields.add(nameDef);
    fields.add(new FieldDef("empty0", "", FieldDef.TYPE_FILLER));
    fields.add(new FieldDef("worldX", "World X", FieldDef.TYPE_INT));
    fields.add(new FieldDef("worldY", "World Y", FieldDef.TYPE_INT));
    fields.add(new FieldDef("worldEi", "EI", FieldDef.TYPE_INT));
    fields.add(new FieldDef("worldRer", "RER", FieldDef.TYPE_INT));
    fields.add(new FieldDef("scanner", "Scanner", FieldDef.TYPE_SCANNER_COMBO));
    fields.add(new FieldDef("notes", "Notes", FieldDef.TYPE_LONG_TEXT));

    fields.add(new FieldDef("empty1", "", FieldDef.TYPE_FILLER));
    fields.add(new FieldDef("empty2", "", FieldDef.TYPE_FILLER));

    //History values for the current turn
    fields.add(new FieldDef("turnLabel", "Turn", FieldDef.TYPE_LABEL));
    fields.add(new FieldDef("empty3", "", FieldDef.TYPE_FILLER));
    fields.add(new FieldDef("owner", "Owner"));
    fields.add(new FieldDef("firepower", "Firepower", FieldDef.TYPE_DOUBLE));
    fields.add(new FieldDef("labour", "Labour", FieldDef.TYPE_INT));
    fields.add(new FieldDef("capital", "Capital", FieldDef.TYPE_INT));

    //History display for the world
    fields.add(new FieldDef("history", "History", FieldDef.TYPE_CUSTOM));

    /**
     * TODO: User can edit values for the turn, but if they navigate to another
     * turn without saving their changes, the values will be lost. Need to do
     * the dirty logic for this too.
     */

    setFields(fields);
  }

  /**
   * The World form and logic have been combined into a single class and this setter is not supported.
   * @throws UnsupportedOperationException
   */
  @Override
  public void setFormLogic(IFormLogic logic) {
    throw new UnsupportedOperationException("Cannot change WorldForm logic impl");
  }

  @Override
  public void inputChanged() {
    ;
  }

  @Override
  public void bind(RomeoForm form, Object record) {
    if(record == null) {
      throw new NullPointerException("record is null");
    }
    if(form != this) {
      throw new IllegalArgumentException("wrong form");
    }
    IWorld world = null;
    if(record instanceof WorldId) {
      world = _worldService.loadWorld( (WorldId)record );
    }
    else if(record instanceof WorldAndHistory) {
      world = ((WorldAndHistory)record).getWorld();
    } else if(record instanceof IWorld) {
      world = (IWorld)record;
    } else {
      throw new ClassCastException("record is not a World or WorldAndHistory: " + record.getClass().getName());
    }
    if(_worldService == null) {
      throw new NullPointerException("worldService is null");
    }
    loadWith(world);
    _worldService.addListener(this);
    _settingsService.addListener(this);

  }

  @Override
  public void cancelChanges() {
    if(_world.isNew()) {
      close();
    } else {
      loadWith(_worldService.loadWorld(_world.getId()));
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
   * Update the form values from the specified World bean. Note that this will
   * also go and retrieve the History data for the world from the to populate
   * @param world
   */
  public void loadWith(IWorld world) {
    _world = world;
    String name = world.getName();
    Map<String, JComponent> fields = getEntryFields();
    ((JTextField) fields.get("name")).setText(name);
    ((JTextField) fields.get("worldX")).setText("" + world.getWorldX());
    ((JTextField) fields.get("worldY")).setText("" + world.getWorldY());
    ((JTextField) fields.get("worldEi")).setText("" + world.getWorldEi());
    ((JTextField) fields.get("worldRer")).setText("" + world.getWorldRer());
    ((ScannerCombo) fields.get("scanner")).setScannerById(world.getScannerId());
    ((JTextArea) fields.get("notes")).setText(world.getNotes());

    setNew(_world.getId() == null);
    if("".equals(name) || name == null) {
      setName("Untitled World");
    } else {
      setName(_world.getId() == null ? "Create New World" : "World - " + name);
    }

    int currentTurn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);
    //Load data for the history table
    List<IHistory> histories;
    if(world.isNew()) {
      histories = Collections.emptyList();
    } else {
      histories = _worldService.loadHistory(world.getId());
    }
    BeanTableModel model = (BeanTableModel) _historyTable.getModel();
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

    setDirty(false);
    dataChanged();
  }

  @Override
  public void saveChanges() {
    Map<String, JComponent> fields = getEntryFields();
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

  @Override
  public void dataChanged(EventObject event) {
    if(event.getSource() instanceof IWorldService) {
      if(_world.getId() != null && !isDirty()) { //Only load fresh data if user hadnt started an edit operation
        IWorldService service = (IWorldService) event.getSource();
        IWorld reloaded = service.loadWorld(_world.getId());
        if(reloaded == null) { //It was deleted (turn this into a new world form with the old values in case they want it back)
          loadWith( new WorldImpl(null,_world) );
          setDirty(true);
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

  /**
   * Detach listener references to allow this form instance to be garbage
   * collected.
   */
  @Override
  public void dispose() {
    _worldService.removeListener(this);
    _settingsService.removeListener(this);
    if(_tnm != null) {
      _tnm.stopListening();
    }
  }

  /**
   * Creates the history table.
   * @return scrollPane a scrollPane which the history table display inside
   */
  @Override
  public JComponent initCustom(FieldDef field) {
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
      _historyTable = table;

      //Make it so clicking on a row in the table , opens the relevant player's form
      NavigatorPanel navigatorPanel = Romeo.getMainFrame().getNavigatorPanel();
      IPlayerService playerService = Romeo.CONTEXT.getPlayerService();
      WorldFormTableListener listener = new WorldFormTableListener(navigatorPanel, playerService);
      _tnm = new TableNavigatorMediator(table, listener);

      return scrollPane;
    }
    return null;
  }
}
