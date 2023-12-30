package romeo.ui;

import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import romeo.battle.BattleCalculatorFactory;
import romeo.battle.ui.BattlePanel;
import romeo.importdata.impl.WorldImporterFactory;
import romeo.players.api.IPlayerService;
import romeo.players.ui.PlayerFormFactory;
import romeo.scenarios.api.IScenarioService;
import romeo.settings.api.ISettingsService;
import romeo.units.api.IUnitService;
import romeo.units.ui.UnitFormFactory;
import romeo.units.ui.UnitGraphsPanel;
import romeo.utils.events.IEventHub;
import romeo.worlds.api.IWorldService;
import romeo.worlds.impl.HistoryChartsHelper;
import romeo.worlds.ui.WorldFormFactory;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.ui.XFactorFormFactory;

/**
 * We need to delay creation of the mainframe until after the initialisers done, so this factory class will
 * keep track of the dependencies and create it when invoked at the appropriate time.
 */
public class MainFrameFactory {
  private final DataSource _dataSource;
  private final IWorldService _worldService;
  private final IUnitService _unitService;
  private final ISettingsService _settingsService;
  private final IPlayerService _playerService;
  private final IScenarioService _scenarioService;
  private final NavigatorPanel _navigatorPanel;
  private final GenericMap _worldsMap; 
  private final IEventHub _shutdownNotifier;
  private final List<String> _worldColumns;
  private final List<String> _unitColumns;
  private final WorldFormFactory _worldFormFactory;
  private final UnitFormFactory _unitFormFactory;
  private final PlayerFormFactory _playerFormFactory;
  private final XFactorFormFactory _xFactorFormFactory;
  private final WorldImporterFactory _worldImporterFactory;
  private final IXFactorService _xFactorService;
  private final IXFactorCompiler _xFactorCompiler;
  private final BattleCalculatorFactory _battleCalculatorFactory;
  
  public MainFrameFactory(
      NavigatorPanel navigatorPanel,
      GenericMap worldsMap,
      IUnitService unitService,
      ISettingsService settingsService,
      IPlayerService playerService,
      IWorldService worldService,
      IEventHub shutdownNotifier,
      List<String> worldColumns,
      List<String> unitColumns,
      IScenarioService scenarioService,
      DataSource dataSource,
      WorldFormFactory worldFormFactory,
      UnitFormFactory unitFormFactory,
      PlayerFormFactory playerFormFactory,
      XFactorFormFactory xFactorFormFactory,
      WorldImporterFactory worldImporterFactory,
      IXFactorService xFactorService,
      IXFactorCompiler xFactorCompiler,
      BattleCalculatorFactory battleCalculatorFactory)
  {
    _navigatorPanel = Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    _worldsMap = Objects.requireNonNull(worldsMap, "worldsMap must not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService must not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService must not be null");
    _shutdownNotifier = Objects.requireNonNull(shutdownNotifier, "shutdownNotifier must not be null");
    _dataSource = Objects.requireNonNull(dataSource, "dataSource must not be null");   
    _worldService = Objects.requireNonNull(worldService, "worldService must not be null");   
    _worldColumns = Objects.requireNonNull(worldColumns, "worldColumns must not be null");
    _unitColumns = Objects.requireNonNull(unitColumns, "unitColumns must not be null");
    _scenarioService = Objects.requireNonNull(scenarioService, "scenarioService must not be null");
    _worldFormFactory = Objects.requireNonNull(worldFormFactory, "worldFormFactory must not be null");
    _unitFormFactory = Objects.requireNonNull(unitFormFactory, "unitFormFactory must not be null");
    _playerFormFactory = Objects.requireNonNull(playerFormFactory, "playerFormFactory must not be null");
    _xFactorFormFactory = Objects.requireNonNull(xFactorFormFactory, "xFactorFormFactory must not be null");
    _worldImporterFactory = Objects.requireNonNull(worldImporterFactory, "worldImporterFactory must not be null");
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService must not be null");
    _xFactorCompiler = Objects.requireNonNull(xFactorCompiler, "xFactorCompiler must not be null");
    _battleCalculatorFactory = Objects.requireNonNull(battleCalculatorFactory, "battleCalculatorFactory must not be null");
  }
  
  public MainFrame createMainFrame() {
    return new MainFrame(
      _navigatorPanel,
      _worldsMap, 
      new UnitGraphsPanel(_unitService), 
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
      _xFactorFormFactory,
      _worldImporterFactory);
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
}
