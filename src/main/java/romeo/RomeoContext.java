package romeo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import romeo.battle.IBattleCalculator;
import romeo.battle.impl.BattleCalculatorImpl;
import romeo.battle.ui.BattlePanel;
import romeo.importdata.IUnitImporter;
import romeo.importdata.IWorldImporter;
import romeo.importdata.impl.AdjustmentsFileReader;
import romeo.importdata.impl.UnitImporterImpl;
import romeo.importdata.impl.WorldImporterImpl;
import romeo.importdata.impl.XFactorFileReader;
import romeo.model.api.IServiceInitialiser;
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
import romeo.settings.ui.PreferencesControls;
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
    Romeo romeo = new Romeo();
    romeo.setDataSource(_dataSource);
    
    List<IServiceInitialiser> initialisers = new ArrayList<>();
    initialisers.add(new HsqldbSettingsInitialiser());
    initialisers.add(new SettingsServiceInitialiser());
    initialisers.add(new WorldServiceInitialiser(_keyGen, _settingsService));
    initialisers.add(new UnitServiceInitialiser(new UnitImporterImpl(_unitService), getUnitColumns(), new AdjustmentsFileReader()));
    initialisers.add(new XFactorServiceInitialiser(_unitService, new XFactorFileReader(), _keyGen));
    initialisers.add(new PlayerServiceInitialiser(_keyGen));
    initialisers.add(new ScenarioServiceInitialiser());    
    romeo.setInitialisers(initialisers);
    return romeo;
  }
  
  /**
   * Once the MainFrame has been displayed, Romeo will use this object to centre the map on the coordinates that
   * were saved in the settings, and to select the origin world that was saved. 
   * @return mapCenterer
   */
  public MapCenterer getMapCenterer() {
    return _mapCenterer;
  }

  public IScenarioService getScenarioService() {
    return _scenarioService;
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
  
  public IEventHub getShutdownNotifier() {
	  return _shutdownNotifier;
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
   * Returns the standard implementation of the {@link IUnitImporter} interface This is
   * configured in the Spring context under "unitImporter". 
   * @return unitImporter
   * @return
   */
  public IUnitImporter createUnitImporter() {
    return new UnitImporterImpl(_unitService);
  }

  /**
   * Returns the order in which world properties are read from the columns of a
   * csv file. Not all column names are actual properties, it is up to the
   * importer to decide whether it ignores or complains when these are
   * encountered. Currently we configure this under the "worldCsvColumns" bean
   * in context.xml
   * @return columnNames names of properties to which worlds.csv columns are
   *         mapped
   */
  public List<String> getWorldColumns() {
    return Arrays.asList("worldID", "name", "worldX", "worldY", "worldEi", "worldRer", "ownerID", "owner", "ownerRace", "class", "labour", "capital", "firepower", "team");
    /*
<!-- Defines the default order of the properties mapped to columns in the map.csv
       If the column order in the csv changes, this is what you need to change so the
       ImportMapAction can read the data correctly.
       nb: Dont change the actual names used for the columns here or it wont be able to import.
       Apparently UC only exports a team column if it is a team game. Romeo currently understands that
       the team column may be missing, but this only works if the team column is the last column
       and if uc changes it so team column precedes another column then we will have a situation where
       different game types have a different column order. You would need to edit this to add or remove
       the team column in accordance with what type of csv you are importing. Thankfully this is not
       currently the case. -->
  <bean id="worldCsvColumns" scope="singleton" class="java.util.ArrayList">
    <constructor-arg><list>
      <value>worldID</value><!-- nb World doesnt use this property -->
      <value>name</value>
      <value>worldX</value>
      <value>worldY</value>
      <value>worldEi</value>
      <value>worldRer</value>
      <value>ownerID</value><!-- nb World doesnt use this property -->
      <value>owner</value> <!-- player name -->
      <value>ownerRace</value>
      <value>class</value> <!-- homeworld | nobody -->
      <value>labour</value> <!-- pop -->
      <value>capital</value> <!-- ult -->
      <value>firepower</value> <!-- visible fp only -->
      <value>team</value> <!-- currently used to import player only. numeric 0=none (but romeo can handle strings here -->
      <!--<value>scanner</value>--> <!-- uc still doesn't provide us this essential information :-( -->
    </list></constructor-arg>
  </bean>
     */
  }

  public List<String> getUnitColumns() {
    //TODO - this needs to be read from a file (again) so that users can tweak it themselves
    //       if necessary. Previously was in the Spring context xml but are removing that file.
    return Arrays.asList("name", "firepower", "maximum","offense", "defense", "attacks", "pd", "carry", "speed", "complexity", "basePrice", "cost", "license", "unitId", "turnAvailable", "stealth", "scanner");
    
    /*
  <!-- Defines the default order of the properties mapped to columns in the units.csv
       If the order of columns in the unit.csv is changed then this will need to be
       reordered accordingly. Dont change the actual names used here as they are the names
       of properties mapped internally -->
  <bean id="unitCsvColumns" scope="singleton" class="java.util.ArrayList">
    <constructor-arg><list>
      <value>name</value>
      <value>firepower</value>
      <value>maximum</value>
      <value>offense</value>
      <value>defense</value>
      <value>attacks</value>
      <value>pd</value>
      <value>carry</value>
      <value>speed</value>
      <value>complexity</value>
      <value>basePrice</value>
      <value>cost</value>
      <value>license</value>
      <value>unitId</value> <!-- unused by Romeo -->
      <value>turnAvailable</value> <!-- unused by Romeo -->
      <value>stealth</value> <!-- unused by Romeo -->
      <value>scanner</value>
    </list></constructor-arg>
  </bean>
     */
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
        _scenarioService);
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
		_shutdownNotifier);
  }

  public PreferencesControls createPreferencesControls() {
    return new PreferencesControls(_settingsService, _scenarioService);
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

  /**
   * Returns a new object that implements {@link IBattleCalculator}
   * @return calculator
   */
  public IBattleCalculator createBattleCalculator() {
    return new BattleCalculatorImpl(_xFactorCompiler);
  }
}
