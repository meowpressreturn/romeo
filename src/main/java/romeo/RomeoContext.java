package romeo;

import java.util.ArrayList;
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
import romeo.players.ui.PlayerFormLogic;
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
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.NumericFieldConstraint;
import romeo.ui.forms.RomeoForm;
import romeo.units.api.IUnitService;
import romeo.units.impl.UnitServiceImpl;
import romeo.units.impl.UnitServiceInitialiser;
import romeo.units.ui.UnitFormLogic;
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
import romeo.worlds.ui.WorldForm;
import romeo.worlds.ui.WorldMapLogic;
import romeo.worlds.ui.WorldNavigatorRecordSelectionListener;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.impl.ExpressionParserImpl;
import romeo.xfactors.impl.XFactorCompilerImpl;
import romeo.xfactors.impl.XFactorServiceImpl;
import romeo.xfactors.impl.XFactorServiceInitialiser;
import romeo.xfactors.ui.XFactorFormLogic;

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
    _worldsMap = initWorldsMap(_worldService, _unitService, _settingsService, _playerService, _shutdownNotifier, _navigatorPanel);
    _mapCenterer = new MapCenterer(_settingsService, _worldService, _worldsMap);
    _worldColumns = Arrays.asList("worldID", "name", "worldX", "worldY", "worldEi", "worldRer", "ownerID", "owner", "ownerRace", "class", "labour", "capital", "firepower", "team");
    _unitColumns = Arrays.asList("name", "firepower", "maximum","offense", "defense", "attacks", "pd", "carry", "speed", "complexity", "basePrice", "cost", "license", "unitId", "turnAvailable", "stealth", "scanner");
    _battleCalculatorFactory = new BattleCalculatorFactory(_xFactorCompiler);
    addLogThreadListeners();
  }
  
  private GenericMap initWorldsMap(
      IWorldService worldService,
      IUnitService unitService,
      ISettingsService settingsService,
      IPlayerService playerService,
      IEventHub shutdownNotifier,
      NavigatorPanel navigatorPanel) {
    IMapLogic logic = new WorldMapLogic(worldService, unitService, settingsService, playerService); 
    IRecordSelectionListener listener = new WorldNavigatorRecordSelectionListener(navigatorPanel);
    GenericMap worldsMap = new GenericMap(logic, listener, shutdownNotifier);
    worldsMap.setFont(new java.awt.Font("Arial", 0, 10));
    return worldsMap;
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

  /**
   * Returns a reference to the DataSource that Romeo is using for persistence.
   * @return datasource
   */
  public DataSource getDataSource() {
    return _dataSource;
  }
  
  public GenericMap getWorldsMap() {
    return _worldsMap;
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
		_worldService, 
		_unitService,
		_shutdownNotifier,
		_worldColumns,
		_unitColumns,
		_scenarioService);
  }

  public RomeoForm createPlayerForm() {
  //TODO - create a class to wrap the below
    RomeoForm form = new RomeoForm();
    form.setName("Player");
    form.setFormLogic(new PlayerFormLogic(_playerService, _worldService, _settingsService));
    List<FieldDef> fields = new ArrayList<FieldDef>();
    
    //name
    FieldDef name = new FieldDef("name","Name");
    name.setMandatory(true);
    fields.add(name);
    
    //color
    FieldDef color = new FieldDef("color","Colour", FieldDef.TYPE_COLOR);
    color.setDefaultValue("255,0,0");
    fields.add(color);
    
    //status
    fields.add(new FieldDef("status","Status"));
    
    //team
    fields.add(new FieldDef("team","Team"));
    
    //notes
    FieldDef notes = new FieldDef("notes","Notes", FieldDef.TYPE_LONG_TEXT);
    notes.setWide(true);
    fields.add(notes);
    
    //turn
    FieldDef turn = new FieldDef("turn","Turn", FieldDef.TYPE_LABEL);
    turn.setWide(true);
    fields.add(turn);
    
    //totalFirepower
    fields.add(new FieldDef("totalFirepower","Firepower", FieldDef.TYPE_LABEL));
    
    //worldCount
    fields.add(new FieldDef("worldCount","Worlds", FieldDef.TYPE_LABEL));
    
    //totalLabour
    fields.add(new FieldDef("totalLabour","Labour", FieldDef.TYPE_LABEL));
    
    //totalCapital
    fields.add(new FieldDef("totalCapital", "Capital", FieldDef.TYPE_LABEL));
    
    form.setFields(fields);
    
    return form;
  }

  public RomeoForm createUnitForm() {
  //TODO - create a class to wrap the below
    RomeoForm form = new RomeoForm();
    form.setName("Unit");
    form.setFormLogic(new UnitFormLogic(_unitService, _xFactorService));
    List<FieldDef> fields = new ArrayList<FieldDef>();

    //name
    fields.add(new FieldDef("name","Name"));
    
    //firepower
    FieldDef firepower = new FieldDef("firepower", "Firepower", FieldDef.TYPE_LABEL);
    firepower.setDefaultValue("0");
    fields.add(firepower);
    
    //acronym
    FieldDef acronym = new FieldDef("acronym","Acronym");
    acronym.setMandatory(true);
    fields.add(acronym);
    
    //empty1
    fields.add(new FieldDef("empty1","", FieldDef.TYPE_CUSTOM));
    
    //attacks
    FieldDef attacks = new FieldDef("attacks","Attacks",FieldDef.TYPE_INT);
    NumericFieldConstraint attacksDetails = new NumericFieldConstraint();
    attacksDetails.setNegativeAllowed(false);
    attacks.setDetails(attacksDetails);
    fields.add(attacks);
    
    //offense
    FieldDef offense = new FieldDef("offense","Offense", FieldDef.TYPE_INT);
    NumericFieldConstraint offenseDetails = new NumericFieldConstraint();
    offenseDetails.setNegativeAllowed(false);
    offenseDetails.setMaxValue(100);
    offense.setDetails(offenseDetails);
    fields.add(offense);
    
    //defense
    FieldDef defense = new FieldDef("defense","Defense", FieldDef.TYPE_INT);
    NumericFieldConstraint defenseDetails = new NumericFieldConstraint();
    defenseDetails.setNegativeAllowed(false);
    defenseDetails.setMaxValue(100);
    defense.setDetails(defenseDetails);
    fields.add(defense);
    
    //pd
    FieldDef pd = new FieldDef("pd","Pop Damage", FieldDef.TYPE_INT);
    NumericFieldConstraint pdDetails = new NumericFieldConstraint();
    pdDetails.setNegativeAllowed(false);
    pd.setDetails(pdDetails);
    fields.add(pd);
    
    //speed
    FieldDef speed = new FieldDef("speed","Speed", FieldDef.TYPE_INT);
    NumericFieldConstraint speedDetails = new NumericFieldConstraint();
    speedDetails.setNegativeAllowed(false);
    speed.setDetails(speedDetails);
    fields.add(speed);
    
    //carry
    fields.add(new FieldDef("carry","Carry", FieldDef.TYPE_INT));
    
    //cost
    FieldDef cost = new FieldDef("cost","Cost", FieldDef.TYPE_INT);
    NumericFieldConstraint costDetails = new NumericFieldConstraint();
    costDetails.setNegativeAllowed(false);
    cost.setDetails(costDetails);
    fields.add(cost);
    
    //complexity
    FieldDef complexity = new FieldDef("complexity","Complexity", FieldDef.TYPE_INT);
    NumericFieldConstraint complexityDetails = new NumericFieldConstraint();
    complexityDetails.setNegativeAllowed(false);
    complexity.setDetails(complexityDetails);
    fields.add(complexity);
    
    //license
    FieldDef license = new FieldDef("license","License", FieldDef.TYPE_INT);
    NumericFieldConstraint licenseDetails = new NumericFieldConstraint();
    licenseDetails.setNegativeAllowed(false);
    license.setDetails(licenseDetails);
    fields.add(license);
    
    //scanner
    FieldDef scanner = new FieldDef("scanner","Scanner", FieldDef.TYPE_INT);
    NumericFieldConstraint scannerDetails = new NumericFieldConstraint();
    scannerDetails.setNegativeAllowed(false);
    scanner.setDetails(scannerDetails);
    fields.add(scanner);
    
    //xfactor
    fields.add( new FieldDef("xfactor","X-Factor", FieldDef.TYPE_XFACTOR_COMBO) );
    
    form.setFields(fields);
    return form;
  }

  public RomeoForm createXFactorForm() {
    //TODO - create a class to wrap the below
    RomeoForm form = new RomeoForm();
    form.setName("X-Factor");
    form.setFormLogic(new XFactorFormLogic(_xFactorService));
    List<FieldDef> fields = new ArrayList<FieldDef>();
    
    //name
    FieldDef name = new FieldDef("name","Name");
    name.setMandatory(true);
    fields.add(name);
    form.setFields(fields);
    
    //description
    fields.add(new FieldDef("description","Description"));
    
    //trigger
    fields.add(new FieldDef("trigger","Trigger", FieldDef.TYPE_EXPRESSION));
    
    //xfAttacks
    fields.add(new FieldDef("xfAttacks","Attacks", FieldDef.TYPE_EXPRESSION));
    
    //xfOffence
    fields.add(new FieldDef("xfOffense","Offense", FieldDef.TYPE_EXPRESSION));
    
    //xfDefense
    fields.add(new FieldDef("xfDefense","Defense", FieldDef.TYPE_EXPRESSION));
    
    //xfPd
    fields.add(new FieldDef("xfPd","PD", FieldDef.TYPE_EXPRESSION));
    
    //xfRemove
    fields.add(new FieldDef("xfRemove","Destruct", FieldDef.TYPE_EXPRESSION));

    return form;
  }

  public WorldForm createWorldForm() {
    return new WorldForm(_worldService, _settingsService);
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
