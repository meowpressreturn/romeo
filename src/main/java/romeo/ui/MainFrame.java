package romeo.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.Romeo;
import romeo.battle.ui.BattlePanel;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.ui.actions.AboutAction;
import romeo.ui.actions.FindWorldAction;
import romeo.ui.actions.ImportUnitsAction;
import romeo.ui.actions.ImportWorldsAction;
import romeo.ui.actions.NewPlayerAction;
import romeo.ui.actions.NewUnitAction;
import romeo.ui.actions.NewWorldAction;
import romeo.ui.actions.NewXFactorAction;
import romeo.ui.actions.OpenPreferencesAction;
import romeo.units.ui.UnitGraphsPanel;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.GuiUtils;
import romeo.utils.events.IEventHub;
import romeo.utils.events.ShutdownEvent;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldAndHistory;

/**
 * This is the main window for Romeo. Its is also responsible for instantiating
 * much of its content.
 */
public class MainFrame extends JFrame {
  public static final String TAB_NAME_DATA = "Data";
  public static final String TAB_NAME_MAP = "Map";
  public static final String TAB_NAME_ANALYSIS = "Analysis";
  public static final String TAB_NAME_GRAPHS = "Graphs";
  public static final String TAB_NAME_SIMULATOR = "Simulator";

  ////////////////////////////////////////////////////////////////////////////

  protected JSplitPane _mainSplitPane = new JSplitPane();
  protected NavigatorPanel _navigatorPanel;
  protected GenericMap _worldsMap;
  protected UnitGraphsPanel _unitGraphsPanel;
  protected GraphsPanel _graphsPanel;
  protected BattlePanel _battlePanel;
  protected JTabbedPane _leftTabs;
  protected DataTabs _dataTabs;
  protected ISettingsService _settingsService;
  protected IEventHub _shutdownNotifier;
  protected String[] _worldColumns;

  /**
   * Constructor. All dependendencies must be provided.
   */
  public MainFrame(NavigatorPanel navigatorPanel,
                   GenericMap worldsMap,
                   UnitGraphsPanel unitGraphsPanel,
                   GraphsPanel graphsPanel,
                   BattlePanel battlePanel,
                   ISettingsService settingsService,
                   IWorldService worldService,
                   IEventHub shutdownNotifier,
                   List<String> worldColumns) {
    Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    Objects.requireNonNull(worldsMap, "worldsMap must not be null");
    Objects.requireNonNull(unitGraphsPanel, "unitGraphsPanel must not be null");
    Objects.requireNonNull(battlePanel, "battlePanel must not be null");
    Objects.requireNonNull(settingsService, "settingsService must not be null");
    Objects.requireNonNull(worldService, "worldService must not be null");
    Objects.requireNonNull(shutdownNotifier, "shutdownNotifier must not be null");
    Objects.requireNonNull(worldColumns, "worldColumns must not be null");
    
    //Log log = LogFactory.getLog(this.getClass());

    _navigatorPanel = navigatorPanel;
    _worldsMap = worldsMap;
    _unitGraphsPanel = unitGraphsPanel;
    _graphsPanel = graphsPanel;
    _battlePanel = battlePanel;
    _settingsService = settingsService;
    _shutdownNotifier = shutdownNotifier;
    _worldColumns = Convert.toStrArray(worldColumns);

    ImageIcon dataIcon = GuiUtils.getImageIcon("/images/data.gif");
    ImageIcon mapIcon = GuiUtils.getImageIcon("/images/map.gif");
    ImageIcon analysisIcon = GuiUtils.getImageIcon("/images/analysis.gif");
    ImageIcon graphsIcon = GuiUtils.getImageIcon("/images/graphs.gif");
    ImageIcon battleIcon = GuiUtils.getImageIcon("/images/battle.gif");

    setIconImage(GuiUtils.getImageIcon("/images/romeo.gif").getImage());
    setTitle("Romeo " + Romeo.ROMEO_VERSION + " (" + Romeo.ROMEO_EDITION + ")");
    int width = (int) settingsService.getLong(ISettings.WINDOW_WIDTH);
    int height = (int) settingsService.getLong(ISettings.WINDOW_HEIGHT);
    setSize(width, height);
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() { //Call the onClose method when frame closes
      @Override
      public void windowClosing(WindowEvent event) {
        onClose();
      }
    });
    contentPane.add(_mainSplitPane, BorderLayout.CENTER);

    prepareMenus(settingsService, worldService);

    _mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    _mainSplitPane.setOneTouchExpandable(true);
    _mainSplitPane.setRightComponent(_navigatorPanel);

    //Tabbed panel for the tabs that show in the left side of main split pane
    _leftTabs = new JTabbedPane();
    _mainSplitPane.setLeftComponent(_leftTabs);

    //Data tab
    _dataTabs = new DataTabs(settingsService, _navigatorPanel, _shutdownNotifier);
    Romeo.incrementSplashProgress("Services");
    _leftTabs.addTab(TAB_NAME_DATA, dataIcon, _dataTabs, null);

    //Map tab
    Romeo.incrementSplashProgress("Map");
    _leftTabs.addTab(TAB_NAME_MAP, mapIcon, worldsMap.getComponent(), null);

    //Analysis tab
    _leftTabs.addTab(TAB_NAME_ANALYSIS, analysisIcon, unitGraphsPanel, null);

    //Graphs tab
    _leftTabs.addTab(TAB_NAME_GRAPHS, graphsIcon, graphsPanel, null);

    //Simulator tab
    battlePanel.setNavigator(_navigatorPanel);
    JScrollPane battleScroll = new JScrollPane(battlePanel);
    battleScroll.getVerticalScrollBar().setUnitIncrement(16);
    battleScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    _leftTabs.addTab(TAB_NAME_SIMULATOR, battleIcon, battleScroll, null);

    //Set which tab is shown first. This now comes from a setting
    String tabName = _settingsService.getString(ISettings.SELECTED_TAB);
    GuiUtils.setSelectedTab(_leftTabs, tabName);

    setLocationRelativeTo(null); //center it
    //and finally nudge the panes into position (need to use a callback for this)
    int splitPaneWidth = (int) _settingsService.getLong(ISettings.LEFT_PANE_WIDTH);
    SwingUtilities.invokeLater(new SplitPaneMover(_mainSplitPane, splitPaneWidth));
    
    //nb: initial centering of the map is now done by the MapCenterer object
    //    which Romeo.whereforeArtThou invokes on the event thread after the
    //    mainframe is displayed.

  }

  public GenericMap getWorldsMap() {
    return _worldsMap;
  }

  /**
   * Creates and sets the menus of this frame
   */
  protected void prepareMenus(ISettingsService settingsService, IWorldService worldService) {
    
    Action prefsAction = new OpenPreferencesAction(_navigatorPanel);
    Action newWorldAction = new NewWorldAction(_navigatorPanel);
    Action newUnitAction = new NewUnitAction(_navigatorPanel);
    Action newXFactorAction = new NewXFactorAction(_navigatorPanel);
    Action newPlayerAction = new NewPlayerAction(_navigatorPanel);
    Action importUnitsAction = new ImportUnitsAction(this, settingsService, Romeo.CONTEXT);
    Action importMapAction = new ImportWorldsAction(this, settingsService, worldService, _worldColumns);
    Action findWorldAction = new FindWorldAction(_navigatorPanel);

    JMenuBar menuBar = new JMenuBar();

    JMenu menuFile = new JMenu();
    menuFile.setText("File");
    menuFile.setMnemonic('f');
    menuBar.add(menuFile);

    JMenu menuMap = new JMenu();
    menuMap.setText("Map");
    menuMap.setMnemonic('m');
    menuBar.add(menuMap);

    TurnMenus turnMenus = new TurnMenus(settingsService, worldService);
    JMenu menuTurn = turnMenus.getMenu();
    menuBar.add(menuTurn);

    JMenu menuHelp = new JMenu();
    menuHelp.setText("Help");
    menuHelp.setMnemonic('h');
    menuBar.add(menuHelp);

    JMenuItem prefsItem = menuFile.add(prefsAction);
    prefsItem.setMnemonic('o');
    prefsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

    JMenuItem newWorldItem = menuFile.add(newWorldAction);
    newWorldItem.setMnemonic('w');
    newWorldItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));

    JMenuItem newUnitItem = menuFile.add(newUnitAction);
    newUnitItem.setMnemonic('u');
    newUnitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));

    JMenuItem newXFactorItem = menuFile.add(newXFactorAction);
    newXFactorItem.setMnemonic('t');
    newXFactorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));

    JMenuItem newPlayerItem = menuFile.add(newPlayerAction);
    newPlayerItem.setMnemonic('p');
    newPlayerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

    JMenuItem importUnitsItem = menuFile.add(importUnitsAction);
    importUnitsItem.setMnemonic('d');
    importUnitsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));

    JMenuItem importMapItem = menuFile.add(importMapAction);
    importMapItem.setMnemonic('m');
    importMapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

    //Map menu
    JMenuItem findWorldItem = menuMap.add(findWorldAction);
    findWorldItem.setMnemonic('f');
    findWorldItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));

    JMenuItem importMapItem2 = menuMap.add(importMapAction);
    importMapItem2.setMnemonic('m');
    importMapItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

    //Help menu      
    JMenuItem aboutItem = menuHelp.add(new AboutAction(this, "About Romeo", "help/about.txt"));
    aboutItem.setMnemonic('a');

    JMenuItem issuesItem = menuHelp.add(new AboutAction(this, "Known Issues", "help/issues.txt"));
    issuesItem.setMnemonic('i');

    JMenuItem dataItem = menuHelp.add(new AboutAction(this, "Data Records", "help/data.txt"));
    dataItem.setMnemonic('d');

    JMenuItem importingUnitsItem = menuHelp
        .add(new AboutAction(this, "Importing Unit Data", "help/importingUnits.txt"));
    importingUnitsItem.setMnemonic('c');

    JMenuItem usingWorldsItem = menuHelp.add(new AboutAction(this, "Using the Worlds Map", "help/worldsMap.txt"));
    usingWorldsItem.setMnemonic('m');

    JMenuItem importMapHelpItem = menuHelp.add(new AboutAction(this, "Importing Map Data", "help/importingMap.txt"));
    importMapHelpItem.setMnemonic('p');

    JMenuItem usingUnitInfoItem = menuHelp.add(new AboutAction(this, "The Unit Info Tab", "help/unitInfo.txt"));
    usingUnitInfoItem.setMnemonic('u');

    JMenuItem usingSimulatorItem = menuHelp.add(new AboutAction(this, "Using the Simulator", "help/simulator.txt"));
    usingSimulatorItem.setMnemonic('s');

    JMenuItem usingXFactorsItem = menuHelp.add(new AboutAction(this, "X-Factors", "help/xFactors.txt"));
    usingXFactorsItem.setMnemonic('x');

    JMenuItem exprTutItem = menuHelp.add(new AboutAction(this, "Expression Tutorial", "help/expressionTutorial.txt"));
    exprTutItem.setMnemonic('t');

    JMenuItem exprRefItem = menuHelp.add(new AboutAction(this, "Expression Reference", "help/expressionReference.txt"));
    exprRefItem.setMnemonic('r');

    setJMenuBar(menuBar);
  }

  /**
   * Informs shutdown listeners that the application is closing, and will issue
   * a SHUTDOWN instruction to the database and then call System.exit(0) Now
   * also uses the settings service to persist the users window preferences.
   * This method needs to know about the ui things with these settings. Ideally
   * we ought to have them listen and persist it themselves instead.
   */
  private void onClose() {
    Log log = LogFactory.getLog(this.getClass());
    log.info("onClose() invoked");

    //Inform shutdown listeners of impending shutdown
    _shutdownNotifier.notifyListeners(new ShutdownEvent(this));

    //TODO - the below should be done in a shutdown listener so onClose is only responsible
    // for sending the notification and performing shutdown

    int windowWidth = this.getWidth();
    int windowHeight = this.getHeight();
    int leftPaneWidth = _mainSplitPane.getDividerLocation();
    _settingsService.setLong(ISettings.WINDOW_WIDTH, windowWidth);
    _settingsService.setLong(ISettings.WINDOW_HEIGHT, windowHeight);
    _settingsService.setLong(ISettings.LEFT_PANE_WIDTH, leftPaneWidth);
    log.debug("Saved window settings: " + windowWidth + "," + windowHeight + "," + leftPaneWidth);

    Point mapCentre = _worldsMap.getVisibleMapCentre();
    int mapX = (int) mapCentre.getX();
    int mapY = (int) mapCentre.getY();
    _settingsService.setLong(ISettings.MAP_X, mapX);
    _settingsService.setLong(ISettings.MAP_Y, mapY);
    WorldAndHistory origin = (WorldAndHistory)_worldsMap.getOrigin();
    String originId = (origin == null) ? "" : origin.getWorld().getId().toString();
    _settingsService.setString(ISettings.MAP_ORIGIN, originId);
    log.debug("Saved world map position: " + mapX + "," + mapY + "," + originId);

    String tabName = GuiUtils.getSelectedTab(_leftTabs);
    _settingsService.setString(ISettings.SELECTED_TAB, tabName);
    log.debug("Saved tab selection: " + tabName);

    //nb: database shutdown should occur last so it shouldnt be called as a listener
    //    where there are no guarantees of order called.
    DataSource ds = Romeo.CONTEXT.getDataSource();
    try {
      Connection connection = ds.getConnection();
      try {
        log.debug("Shutting down database engine");
        DbUtils.writeQuery("SHUTDOWN", null, connection);
      } finally {
        connection.close();
        log.info("Exiting Romeo");
        System.exit(0);
      }
    } catch(Exception e) {
      log.error(e);
    }
  }

  /**
   * Returns reference to the {@link NavigatorPanel} instance.
   * @return navigatorPanel
   */
  public NavigatorPanel getNavigatorPanel() {
    return _navigatorPanel;
  }

  public JTabbedPane getLeftTabs() {
    return _leftTabs;
  }

  public DataTabs getDataTabs() {
    return _dataTabs;
  }

  /**
   * Set the specified leftTab in the mainframe. The name must be a valid tab
   * title lest an IllegalArgumentException be thrown.
   * @param title
   * @throws IllegalArgumentException
   */
  public void setSelectedTab(String title) {
    boolean ok = GuiUtils.setSelectedTab(_leftTabs, title);
    if(!ok) {
      throw new IllegalArgumentException("Tab not found:" + title);
    }
  }

}
