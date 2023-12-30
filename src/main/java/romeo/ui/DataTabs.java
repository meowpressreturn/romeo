package romeo.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.Romeo;
import romeo.players.api.IPlayerService;
import romeo.players.ui.PlayerFormFactory;
import romeo.players.ui.PlayerNavigatorRecordSelectionListener;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.ui.UnitFormFactory;
import romeo.units.ui.UnitNavigatorRecordSelectionListener;
import romeo.utils.GuiUtils;
import romeo.utils.events.IEventHub;
import romeo.utils.events.IEventListener;
import romeo.utils.events.ShutdownEvent;
import romeo.worlds.api.IWorldService;
import romeo.worlds.ui.TurnControls;
import romeo.worlds.ui.WorldFormFactory;
import romeo.worlds.ui.WorldNavigatorRecordSelectionListener;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.ui.XFactorFormFactory;
import romeo.xfactors.ui.XFactorNavigatorRecordSelectionListener;

/**
 * The tabbed pane that shows the tables of data records. This class will also
 * create the {@link TableNavigatorMediator} instances that are subsequently
 * available via getters (they open the relevant forms in the other panel when
 * the table gets double-clicked).
 */
public class DataTabs extends JPanel {
  public static final String TAB_NAME_UNITS = "Units";
  public static final String TAB_NAME_XFACTORS = "X-Factors";
  public static final String TAB_NAME_WORLDS = "Worlds";
  public static final String TAB_NAME_PLAYERS = "Players";

  protected TableNavigatorMediator _worldTnm;
  protected TableNavigatorMediator _unitTnm;
  protected TableNavigatorMediator _xFactorTnm;
  private JTabbedPane _tabs;
  private JTable _worldTable;
  private IEventListener _shutdownListener;

  private ISettingsService _settingsService;

  public DataTabs(
      ISettingsService settingsService, 
      IPlayerService playerService,
      NavigatorPanel navigatorPanel,
      IEventHub shutDownNotifier,
      WorldFormFactory worldFormFactory,
      UnitFormFactory unitFormFactory,
      PlayerFormFactory playerFormFactory,
      XFactorFormFactory xFactorFormFactory,
      IXFactorService xFactorService) {
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
    Objects.requireNonNull(navigatorPanel, "navigatorPanel may not be null");
    Objects.requireNonNull(shutDownNotifier, "shutdownNotifier may not be null");
    Objects.requireNonNull(worldFormFactory, "worldFormFactory may not be null");
    Objects.requireNonNull(unitFormFactory, "unitFormFactory may not be null");
    Objects.requireNonNull(xFactorFormFactory, "xFactorFormFactory may not be null");

    //prep worlds table
    IWorldService worldService = Romeo.CONTEXT.getWorldService();
    WorldDataTableModel worldTableModel = new WorldDataTableModel(worldService, settingsService);

    _worldTable = new JTable(worldTableModel);
    //Make name column bigger
    TableColumnModel worldTcm = _worldTable.getColumnModel();
    for(int i = 0; i < worldTcm.getColumnCount(); i++) {
      worldTcm.getColumn(i).setPreferredWidth(i == 0 ? 150 : 50);
    }
    worldTableModel.initColumnClickListener(_worldTable);
    _worldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _worldTnm = new TableNavigatorMediator(_worldTable, new WorldNavigatorRecordSelectionListener(navigatorPanel, worldFormFactory));
    JScrollPane worldsTableScrollPane = new JScrollPane(_worldTable);
    worldsTableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    ImageIcon worldIcon = GuiUtils.getImageIcon("/images/world.gif");
    _worldTable.getTableHeader().setDefaultRenderer(
        new BeanTableHeaderRenderer((DefaultTableCellRenderer) _worldTable.getTableHeader().getDefaultRenderer()));
    GuiUtils.setColumnWidths(_worldTable, new int[] { 128, 64, 32, 48, 32, 32, 48, 48 });

    //Prep the units table
    IUnitService us = Romeo.CONTEXT.getUnitService();

    BeanTableModel.ColumnDef[] unitColumns = new BeanTableModel.ColumnDef[] {
        new BeanTableModel.ColumnDef("name", "Name"), new BeanTableModel.ColumnDef("acronym", "Acr"),
        new BeanTableModel.ColumnDef("speed", "Spd"), new BeanTableModel.ColumnDef("firepower", "FP"),
        new BeanTableModel.ColumnDef("attacks", "Atk"), new BeanTableModel.ColumnDef("offense", "Off"),
        new BeanTableModel.ColumnDef("defense", "Def"), new BeanTableModel.ColumnDef("pd", "PD"),
        new BeanTableModel.ColumnDef("carry", "CA"), new BeanTableModel.ColumnDef("license", "Lic"),
        new BeanTableModel.ColumnDef("complexity", "Cpx"), };
    ServiceTableModel unitTableModel = new ServiceTableModel(unitColumns, us) {
      @Override
      protected List<IUnit> fetchNewData() {
        return new ArrayList<IUnit>(((IUnitService) _service).getUnits());
      }
    };

    JTable unitTable = new JTable(unitTableModel);
    unitTable.setDefaultRenderer(Double.class, new NumericCellRenderer(2));

    GuiUtils.setColumnWidths(unitTable, new int[] { 200, 100 });
    unitTableModel.initColumnClickListener(unitTable);
    unitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _unitTnm = new TableNavigatorMediator(unitTable, new UnitNavigatorRecordSelectionListener(navigatorPanel, unitFormFactory));
    JScrollPane unitsTableScrollPane = new JScrollPane(unitTable);
    unitsTableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    ImageIcon unitIcon = GuiUtils.getImageIcon("/images/unit.gif");
    unitTable.getTableHeader().setDefaultRenderer(
        new BeanTableHeaderRenderer((DefaultTableCellRenderer) unitTable.getTableHeader().getDefaultRenderer()));

    //Prep the xFactors table
    BeanTableModel.ColumnDef[] xfColumns = new BeanTableModel.ColumnDef[] {
        new BeanTableModel.ColumnDef("name", "Name"), new BeanTableModel.ColumnDef("description", "Descripton"),

    };
    ServiceTableModel xfTableModel = new ServiceTableModel(xfColumns, xFactorService) {
      @Override
      protected List<IXFactor> fetchNewData() {
        return new ArrayList<IXFactor>(((IXFactorService) _service).getXFactors());
      }
    };

    JTable xfTable = new JTable(xfTableModel);

    TableColumnModel xfTcm = xfTable.getColumnModel();
    //Make description column bigger
    xfTcm.getColumn(1).setPreferredWidth(245);
    xfTcm.getColumn(0).setPreferredWidth(80);
    xfTableModel.initColumnClickListener(xfTable);
    xfTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _xFactorTnm = new TableNavigatorMediator(xfTable, new XFactorNavigatorRecordSelectionListener(navigatorPanel, xFactorFormFactory));
    JScrollPane xfTableScrollPane = new JScrollPane(xfTable);
    xfTableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    ImageIcon xfIcon = GuiUtils.getImageIcon("/images/xFactor.gif");
    xfTable.getTableHeader().setDefaultRenderer(
        new BeanTableHeaderRenderer((DefaultTableCellRenderer) xfTable.getTableHeader().getDefaultRenderer()));

    //prep players table
    PlayerDataTableModel playerTableModel = new PlayerDataTableModel(playerService, worldService, settingsService);

    JTable playerTable = new JTable(playerTableModel);
    GuiUtils.setColumnWidths(playerTable, new int[] { 150, 32, 48, 48, 48, 48, 100, 64 });
    //Special rendering for colours - which we keep as the last column
    playerTable.getColumnModel().getColumn(playerTable.getColumnModel().getColumnCount() - 1)
        .setCellRenderer(new ColorColumnRenderer());
    playerTable.setDefaultRenderer(Double.class, new NumericCellRenderer(2));
    playerTable.setDefaultRenderer(Integer.class, new NumericCellRenderer(0));

    playerTableModel.initColumnClickListener(playerTable);
    playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    new TableNavigatorMediator(playerTable,
        new PlayerNavigatorRecordSelectionListener(navigatorPanel, playerService, playerFormFactory));
    JScrollPane playerTableScrollPane = new JScrollPane(playerTable);
    playerTableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    ImageIcon playerIcon = GuiUtils.getImageIcon("/images/player.gif");
    playerTable.getTableHeader().setDefaultRenderer(
        new BeanTableHeaderRenderer((DefaultTableCellRenderer) playerTable.getTableHeader().getDefaultRenderer()));

    
    TurnControls turnControls = new TurnControls(settingsService, worldService);
    JPanel turnPanel = new JPanel();
    turnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    turnPanel.add(turnControls.getFirstButton());
    turnPanel.add(turnControls.getPrevButton());
    turnPanel.add(turnControls.getNextButton());
    turnPanel.add(turnControls.getLastButton());
    turnPanel.add(turnControls.getTurnLabel());
    
    //Layout the tabs and components
    _tabs = new JTabbedPane();
    _tabs.addTab(TAB_NAME_PLAYERS, playerIcon, playerTableScrollPane, null);
    _tabs.addTab(TAB_NAME_WORLDS, worldIcon, worldsTableScrollPane, null);
    _tabs.addTab(TAB_NAME_UNITS, unitIcon, unitsTableScrollPane, null);
    _tabs.addTab(TAB_NAME_XFACTORS, xfIcon, xfTableScrollPane, null);    

    String selectedDataTab = _settingsService.getString(ISettings.SELECTED_DATA_TAB);
    GuiUtils.setSelectedTab(_tabs, selectedDataTab);

    //Get notified when romeo is closing so we can save which of the data tabs the user was last using.
    //We are registering it as a weak ref, and since its an anon-inner class we also hold a private reference
    //to it here so it doesnt get collected before the DataTabs itself. (We could also have made DataTabs implement
    //the listener interface and expose the method itself which would get rid of the need for the variable but
    //hang out our dirty listener laundry for all to see...)
    _shutdownListener = new IEventListener() {
      @Override
      public void onEvent(EventObject event) {
        if(event instanceof ShutdownEvent) {
          Log log = LogFactory.getLog(this.getClass());
          String selectedDataTab = GuiUtils.getSelectedTab(_tabs);
          _settingsService.setString(ISettings.SELECTED_DATA_TAB, selectedDataTab);
          log.debug("Saved selectedDataTab: " + selectedDataTab);
        }
      }
    };
    shutDownNotifier.addWeakListener(_shutdownListener);

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = new Insets(3, 0, 0, 0);
    gbc.gridwidth = 4;
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 2;
    gbc.weighty = 2;
    add(_tabs, gbc);
    
    gbc.gridy++;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(turnPanel, gbc);
    
    validate();
  }
}
