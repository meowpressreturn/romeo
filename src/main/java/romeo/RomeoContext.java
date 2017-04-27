package romeo;

import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;

import romeo.battle.IBattleCalculator;
import romeo.importdata.IUnitImporter;
import romeo.importdata.IWorldImporter;
import romeo.players.api.IPlayerService;
import romeo.scenarios.api.IScenarioService;
import romeo.settings.api.ISettingsService;
import romeo.settings.ui.PreferencesControls;
import romeo.ui.MainFrame;
import romeo.ui.forms.ExpressionField;
import romeo.ui.forms.RomeoForm;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;
import romeo.utils.events.IEventHub;
import romeo.worlds.api.IWorldService;
import romeo.worlds.ui.WorldForm;
import romeo.xfactors.api.IXFactorService;

/**
 * Wraps the Spring ApplicationContext and provides specific methods to access the beans Romeo needs.
 */
public class RomeoContext {
  
  //At some point we hope to drop Spring from Romeo in order to reduce the size of the distribution
  //so centralising all access to the context through here is a start.
  
  private ApplicationContext _context;

  /**
   * Subclasses that wish to create a RomeoContext without providing an
   * ApplicationContext reference may use this constructor. They must take
   * responsibility for overriding any methods their clients will use that need
   * it of course.
   */
  protected RomeoContext() {
    _context = null;
  }

  /**
   * Constructor, this requires a reference to a Spring ApplicationContext
   * @param context
   */
  public RomeoContext(ApplicationContext context) {
    _context = Objects.requireNonNull(context);
  }
  
  /**
   * Once the MainFrame has been displayed, Romeo will use this object to centre the map on the coordinates that
   * were saved in the settings, and to select the origin world that was saved. 
   * @return mapCenterer
   */
  public Runnable getMapCenterer() {
    return _context.getBean("mapCenterer", Runnable.class);
  }

  public IScenarioService getScenarioService() {
    return _context.getBean("scenarioService", IScenarioService.class);
  }

  public IPlayerService getPlayerService() {
    return _context.getBean("playerService", IPlayerService.class);
  }

  public ISettingsService getSettingsService() {
    return _context.getBean("settingsService", ISettingsService.class);
  }

  public IUnitService getUnitService() {
    return _context.getBean("unitService", IUnitService.class);
  }

  public IWorldService getWorldService() {
    return _context.getBean("worldService", IWorldService.class);
  }

  public IXFactorService getXFactorService() {
    return _context.getBean("xFactorService", IXFactorService.class);
  }

  public IEventHub getEventHub(String name) {
    IEventHub hub = _context.getBean(name, IEventHub.class);
    return hub;
  }

  /**
   * Returns an object implenting the {@link IWorldImporter} interface. Call
   * this for each import as the importer is a disposable stateful object.
   * @return worldImporter
   */
  public IWorldImporter createWorldImporter() {
    if(_context.isSingleton("worldImporter")) {
      throw new RuntimeException("worldImporter must have prototype scope in context.xml");
    }
    IWorldImporter importer = _context.getBean("worldImporter", IWorldImporter.class);
    return importer;
  }

  /**
   * Returns the standard implementation of the {@link IUnitImporter} interface This is
   * configured in the Spring context under "unitImporter". 
   * @return unitImporter
   * @return
   */
  public IUnitImporter createUnitImporter() {
    if(_context.isSingleton("unitImporter")) {
      throw new RuntimeException("unitImporter must have prototype scope in context.xml");
    }
    IUnitImporter importer = _context.getBean("unitImporter", IUnitImporter.class);
    return importer;
  }

  private List<String> getColumnNames(String beanName) {
    @SuppressWarnings("unchecked")
    List<String> columnNames = _context.getBean(beanName, List.class);
    if(!Convert.isAllClass(columnNames, String.class, false)) {
      throw new IllegalStateException(beanName + " are not all String, check context.xml");
    }
    return columnNames;
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
    return getColumnNames("worldCsvColumns");
  }

  /**
   * Column order for unit.csv Currently we configure this under the
   * "unitCsvColumns" bean in context.xml
   * @return columnNames names of properties to which unit.csv columns are
   *         mapped
   */
  public List<String> getUnitColumns() {
    return getColumnNames("unitCsvColumns");
  }

  /**
   * Returns a reference to the DataSource that Romeo is using for persistence.
   * @return datasource
   */
  public DataSource getDataSource() {
    return _context.getBean("dataSource", DataSource.class);
  }

  /**
   * If you want a reference to the {@link MainFrame} for use in ui code, then
   * you should call the static Romeo.getMainFrame() method. This one in
   * RomeoContext returns a {@link MainFrame} object from the Spring context.
   * Romeo uses it during initialisation to create the mainframe via spring so
   * that all its services are injected.
   * @return
   */
  public MainFrame createMainFrame() {
    return _context.getBean("mainFrame", MainFrame.class);
  }

  /**
   * Creates an instance of the {@link PreferencesControls} with required
   * dependencies injected.
   * @return controls
   */
  public PreferencesControls createPreferencesControls() {
    return _context.getBean("preferencesControls", PreferencesControls.class);
  }

  public RomeoForm createPlayerForm() {
    return _context.getBean("playerForm", RomeoForm.class);
  }

  public RomeoForm createUnitForm() {
    return _context.getBean("unitForm", RomeoForm.class);
  }

  public RomeoForm createXFactorForm() {
    return _context.getBean("xFactorForm", RomeoForm.class);
  }

  public WorldForm createWorldForm() {
    return _context.getBean("worldForm", WorldForm.class);
  }

  /**
   * Create an {@link ExpressionField} with the necessary service references
   * injected.
   * @return
   */
  public ExpressionField createExpressionField() {
    return _context.getBean("expressionField", ExpressionField.class);
  }

  /**
   * Returns a new object that implements {@link IBattleCalculator}
   * @return calculator
   */
  public IBattleCalculator createBattleCalculator() {
    return _context.getBean("battleCalculator", IBattleCalculator.class);
  }
}
