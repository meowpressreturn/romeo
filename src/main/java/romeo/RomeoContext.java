package romeo;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import romeo.battle.BattleCalculatorFactory;
import romeo.battle.ui.BattlePanel;
import romeo.importdata.IWorldImporter;
import romeo.importdata.impl.AdjustmentsFileReader;
import romeo.importdata.impl.UnitImporterImpl;
import romeo.importdata.impl.WorldImporterImpl;
import romeo.importdata.impl.XFactorFileReader;
import romeo.persistence.HsqldbSettingsInitialiser;
import romeo.persistence.QndDataSource;
import romeo.players.api.IPlayerService;
import romeo.players.impl.PlayerServiceImpl;
import romeo.players.impl.PlayerServiceInitialiser;
import romeo.players.ui.PlayerFormFactory;
import romeo.scenarios.api.IScenarioService;
import romeo.scenarios.impl.ScenarioServiceImpl;
import romeo.scenarios.impl.ScenarioServiceInitialiser;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingsServiceImpl;
import romeo.settings.impl.SettingsServiceInitialiser;
import romeo.ui.GenericMap;
import romeo.ui.GenericMap.IMapLogic;
import romeo.ui.GraphsPanel;
import romeo.ui.IRecordSelectionListener;
import romeo.ui.MainFrame;
import romeo.ui.MapCenterer;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.ExpressionField;
import romeo.units.api.IUnitService;
import romeo.units.impl.UnitServiceImpl;
import romeo.units.impl.UnitServiceInitialiser;
import romeo.units.ui.UnitFormFactory;
import romeo.units.ui.UnitGraphsPanel;
import romeo.utils.IKeyGen;
import romeo.utils.KeyGenImpl;
import romeo.utils.LogThreadNameInvocationListener;
import romeo.utils.events.EventHubImpl;
import romeo.utils.events.IEventHub;
import romeo.worlds.api.IWorldService;
import romeo.worlds.impl.HistoryChartsHelper;
import romeo.worlds.impl.WorldServiceImpl;
import romeo.worlds.impl.WorldServiceInitialiser;
import romeo.worlds.ui.WorldFormFactory;
import romeo.worlds.ui.WorldMapLogic;
import romeo.worlds.ui.WorldNavigatorRecordSelectionListener;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.impl.ExpressionParserImpl;
import romeo.xfactors.impl.XFactorCompilerImpl;
import romeo.xfactors.impl.XFactorServiceImpl;
import romeo.xfactors.impl.XFactorServiceInitialiser;
import romeo.xfactors.ui.XFactorFormFactory;

/**
 * Wraps the Spring ApplicationContext and provides specific methods to access the beans Romeo needs.
 * Going forward we are reducing the dependence on Spring with the aim of removing it so this class will
 * take responsbility for creating many of the objects instead.
 */
public class RomeoContext {
  private final IKeyGen _keyGen;
  private final DataSource _dataSource;
  private final IWorldService _worldService;
  private final IUnitService _unitService;
  private final ISettingsService _settingsService;
  private final IPlayerService _playerService;
  private final IScenarioService _scenarioService;
  private final IXFactorService _xFactorService;
  private final IXFactorCompiler _xFactorCompiler;
  private final IExpressionParser _expressionParser;
  private final NavigatorPanel _navigatorPanel;
  private final GenericMap _worldsMap; 
  private final MapCenterer _mapCenterer;
  private final IEventHub _shutdownNotifier;
  private final List<String> _worldColumns;
  private final List<String> _unitColumns;
  private final BattleCalculatorFactory _battleCalculatorFactory;
  private final WorldFormFactory _worldFormFactory;
  private final UnitFormFactory _unitFormFactory;
  private final PlayerFormFactory _playerFormFactory;
  private final XFactorFormFactory _xFactorFormFactory;
 
  public RomeoContext() {    
    QndDataSource ds = new QndDataSource();
    ds.setDriver("org.hsqldb.jdbcDriver");
    ds.setDatabase("jdbc:hsqldb:database/romeo");  //relative location of the database (to be created on first use)
    _dataSource = ds;
    _keyGen = new KeyGenImpl();    
    _unitService = new UnitServiceImpl(_dataSource, _keyGen);
    _settingsService = new SettingsServiceImpl(_dataSource, _keyGen);
    _playerService = new PlayerServiceImpl(_dataSource, _keyGen);
    _worldService = new WorldServiceImpl(_dataSource, _keyGen, _playerService, _unitService, _settingsService);
    _scenarioService = new ScenarioServiceImpl(_dataSource, _keyGen);
    _xFactorService = new XFactorServiceImpl(_dataSource, _keyGen, _unitService);
    _expressionParser = new ExpressionParserImpl();
    _xFactorCompiler = new XFactorCompilerImpl(_expressionParser, _xFactorService);
    _navigatorPanel = new NavigatorPanel();    
    _shutdownNotifier = new EventHubImpl();    
    _unitFormFactory = new UnitFormFactory(_unitService, _xFactorService);
    _playerFormFactory = new PlayerFormFactory(_playerService, _worldService, _settingsService);
    _worldFormFactory = new WorldFormFactory(_worldService, _settingsService, _playerService, _playerFormFactory);
    _xFactorFormFactory = new XFactorFormFactory(_xFactorService);
    
    IMapLogic logic = new WorldMapLogic(_worldService, _unitService, _settingsService, _playerService); 
    IRecordSelectionListener listener = new WorldNavigatorRecordSelectionListener(_navigatorPanel, _worldFormFactory);
    _worldsMap = new GenericMap(logic, listener, _shutdownNotifier);
    _worldsMap.setFont(new java.awt.Font("Arial", 0, 10));
    
    _mapCenterer = new MapCenterer(_settingsService, _worldService, _worldsMap);
    _worldColumns = Arrays.asList("worldID", "name", "worldX", "worldY", "worldEi", "worldRer", "ownerID", "owner", "ownerRace", "class", "labour", "capital", "firepower", "team");
    _unitColumns = Arrays.asList("name", "firepower", "maximum","offense", "defense", "attacks", "pd", "carry", "speed", "complexity", "basePrice", "cost", "license", "unitId", "turnAvailable", "stealth", "scanner");
    _battleCalculatorFactory = new BattleCalculatorFactory(_xFactorCompiler);
    
    addLogThreadListeners();
  }
  
  public Romeo createRomeo() {
    return new Romeo(
        _dataSource,
        _mapCenterer,
        Arrays.asList(
            new HsqldbSettingsInitialiser(),
            new SettingsServiceInitialiser(),
            new WorldServiceInitialiser(_keyGen, _settingsService),
            new UnitServiceInitialiser(new UnitImporterImpl(_unitService), _unitColumns, new AdjustmentsFileReader()),
            new XFactorServiceInitialiser(_unitService, new XFactorFileReader(), _keyGen),
            new PlayerServiceInitialiser(_keyGen),
            new ScenarioServiceInitialiser()));
  }

  public IPlayerService getPlayerService() {
    return _playerService;
  }

  public ISettingsService getSettingsService() {
    return _settingsService;
  }

  public IUnitService getUnitService() {
    return _unitService;
  }

  public IWorldService getWorldService() {
    return _worldService;
  }

  public IXFactorService getXFactorService() {
    return _xFactorService;
  }

  /**
   * Returns an object implenting the {@link IWorldImporter} interface. Call
   * this for each import as the importer is a disposable stateful object.
   * @return worldImporter
   */
  public IWorldImporter createWorldImporter() {
    return new WorldImporterImpl(_worldService, _playerService, _settingsService);
  }
  
  private GraphsPanel createGraphsPanel() {
    
    HistoryChartsHelper worldChartsHelper = new HistoryChartsHelper(_dataSource);
    worldChartsHelper.setPlayerHistorySql(
        "SELECT #STAT# AS value, turn, owner FROM WORLDS_HISTORY GROUP BY owner, turn ORDER BY owner, turn");
    
    worldChartsHelper.setTeamWorldsSql(
        "SELECT COUNT(worldId) AS worlds, turn, team"
        + " FROM WORLDS_HISTORY WH"
        + " JOIN PLAYERS P"
        + " ON WH.owner=P.name"
        + " WHERE team IS NOT NULL"
        + " GROUP BY team,turn"
        + " ORDER BY team,turn");
    
    worldChartsHelper.setTeamFirepowerSql(
        "SELECT h.turn AS turn, p.team AS team, SUM(firepower) AS firepower"
        + " FROM worlds_history h"
        + " JOIN worlds w ON h.worldId=w.id"
        + " JOIN players p ON h.owner=p.name"
        + " GROUP BY team, turn"
        + " ORDER BY team, turn");
    
    worldChartsHelper.setTeamLabourSql(
        "SELECT h.turn AS turn, p.team AS team, SUM(labour) AS labour"
        + " FROM worlds_history h"
        + " JOIN worlds w ON h.worldId=w.id"
        + " JOIN players p ON h.owner=p.name"
        + " GROUP BY team, turn"
        + " ORDER BY team, turn");
    
    worldChartsHelper.setTeamCapitalSql(
        "SELECT h.turn AS turn, p.team AS team, SUM(capital) AS capital"
        + " FROM worlds_history h"
        + " JOIN worlds w ON h.worldId=w.id"
        + " JOIN players p ON h.owner=p.name"
        + " GROUP BY team, turn"
        + " ORDER BY team, turn");
    
	  GraphsPanel graphsPanel = new GraphsPanel(_worldService, _playerService, worldChartsHelper);
	  
	  return graphsPanel;
  }
  
  private BattlePanel createBattlePanel() {
    return new BattlePanel(
        _unitService,
        _settingsService,
        _xFactorService,
        _xFactorCompiler,
        _scenarioService,
        _battleCalculatorFactory,
        _navigatorPanel);
  }
  
  /**
   * If you want a reference to the {@link MainFrame} for use in ui code, then
   * you should call the static Romeo.getMainFrame() method. 
   */
  public MainFrame createMainFrame() {
	  return new MainFrame(
		_navigatorPanel,
		_worldsMap, 
		new UnitGraphsPanel(getUnitService()), 
		createGraphsPanel(), 
		createBattlePanel(), 
		_settingsService, 
		_playerService,
		_worldService, 
		_unitService,
		_shutdownNotifier,
		_worldColumns,
		_unitColumns,
		_scenarioService,
		_dataSource,
		_worldFormFactory,
		_unitFormFactory,
		_playerFormFactory,
		_xFactorFormFactory);
  }

  /**
   * Create an {@link ExpressionField} with the necessary service references
   * injected.
   * @return
   */
  public ExpressionField createExpressionField() {
    return new ExpressionField(_expressionParser);
  }

  private void addLogThreadListeners() {
    _worldService.addListener(new LogThreadNameInvocationListener());
    _unitService.addListener(new LogThreadNameInvocationListener());
    _xFactorService.addListener(new LogThreadNameInvocationListener());
    _playerService.addListener(new LogThreadNameInvocationListener());
    _scenarioService.addListener(new LogThreadNameInvocationListener());
    _settingsService.addListener(new LogThreadNameInvocationListener()); 
  }
}
