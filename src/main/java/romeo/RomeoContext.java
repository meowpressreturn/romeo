package romeo;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import romeo.battle.BattleCalculatorFactory;
import romeo.importdata.impl.AdjustmentsFileReader;
import romeo.importdata.impl.UnitImporterImpl;
import romeo.importdata.impl.WorldImporterFactory;
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
import romeo.ui.IRecordSelectionListener;
import romeo.ui.MainFrameFactory;
import romeo.ui.MapCenterer;
import romeo.ui.NavigatorPanel;
import romeo.units.api.IUnitService;
import romeo.units.impl.UnitServiceImpl;
import romeo.units.impl.UnitServiceInitialiser;
import romeo.units.ui.UnitFormFactory;
import romeo.utils.IKeyGen;
import romeo.utils.KeyGenImpl;
import romeo.utils.LogThreadNameInvocationListener;
import romeo.utils.events.EventHubImpl;
import romeo.utils.events.IEventHub;
import romeo.worlds.api.IWorldService;
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
  private final WorldImporterFactory _worldImporterFactory;
  private final MainFrameFactory _mainFrameFactory;
 
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
    _worldFormFactory = new WorldFormFactory(_worldService, _settingsService, _playerService, _unitService, _playerFormFactory);
    _xFactorFormFactory = new XFactorFormFactory(_xFactorService, _expressionParser);
    _worldImporterFactory = new WorldImporterFactory(_worldService, _playerService, _settingsService);
    
    IMapLogic logic = new WorldMapLogic(_worldService, _unitService, _settingsService, _playerService); 
    IRecordSelectionListener listener = new WorldNavigatorRecordSelectionListener(_navigatorPanel, _worldFormFactory);
    _worldsMap = new GenericMap(logic, listener, _shutdownNotifier);
    _worldsMap.setFont(new java.awt.Font("Arial", 0, 10));
    
    _mapCenterer = new MapCenterer(_settingsService, _worldService, _worldsMap);
    _worldColumns = Arrays.asList("worldID", "name", "worldX", "worldY", "worldEi", "worldRer", "ownerID", "owner", "ownerRace", "class", "labour", "capital", "firepower", "team");
    _unitColumns = Arrays.asList("name", "firepower", "maximum","offense", "defense", "attacks", "pd", "carry", "speed", "complexity", "basePrice", "cost", "license", "unitId", "turnAvailable", "stealth", "scanner");
    _battleCalculatorFactory = new BattleCalculatorFactory(_xFactorCompiler);
    
    _mainFrameFactory = new MainFrameFactory(
        _navigatorPanel, 
        _worldsMap, 
        _unitService, 
        _settingsService, 
        _playerService, 
        _worldService, 
        _shutdownNotifier, 
        _worldColumns, 
        _unitColumns, 
        _scenarioService, 
        _dataSource, 
        _worldFormFactory, 
        _unitFormFactory, 
        _playerFormFactory, 
        _xFactorFormFactory, 
        _worldImporterFactory, 
        _xFactorService, 
        _xFactorCompiler, 
        _battleCalculatorFactory);
    
    addLogThreadListeners();
  }
  
  public Romeo layOurScene() {
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
            new ScenarioServiceInitialiser()),
        _mainFrameFactory);
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
