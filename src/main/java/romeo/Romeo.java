package romeo;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.slf4j.LoggerFactory;

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
import romeo.ui.ErrorDialog;
import romeo.ui.GenericMap;
import romeo.ui.GenericMap.IMapLogic;
import romeo.ui.forms.RomeoFormInitialiser;
import romeo.ui.IRecordSelectionListener;
import romeo.ui.MainFrame;
import romeo.ui.MainFrameFactory;
import romeo.ui.MapCenterer;
import romeo.ui.NavigatorPanel;
import romeo.units.api.IUnitService;
import romeo.units.impl.UnitServiceImpl;
import romeo.units.impl.UnitServiceInitialiser;
import romeo.units.ui.UnitFormFactory;
import romeo.utils.ClassPathFile;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
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

/**
 * Contains the main method and starts the application by constructing and invoking the Romeo object. 
 */
public class Romeo {
  static {
    fixJFreeChartStyle();
  }

  //Nb: When releasing a new version, you need to update the version here, in the build.xml
  //    in the help texts and readMe.txt 
  //    You probably also need to update the copyright dates in these places.

  public static final String ROMEO_EDITION = "Havana"; //Can cover several similar versions
  public static final String ROMEO_VERSION = "0.6.4"; //Specific release version (dont forget build.xml too!)
  public static final String LICENSE = "<html><center>" //License disclaimer in NavigatorPanel
      + "<br><br>"

      + "Welcome to Romeo (" + ROMEO_EDITION + " Edition v" + ROMEO_VERSION + ")<br>"
      + "A strategy analysis tool for players of UltraCorps.<br><br>"

      + "UltraCorps is copyright &copy; 1998-2002, 2005-2024 by<br>"
      + "Steve Jackson Games. UltraCorps is a trademark of Steve<br>"
      + "Jackson Games, which reserves all rights. This program was<br>"
      + "created and distributed by permission of Steve Jackson Games.<br>" + "Conquest is Job One!<br><br>"

      + "Romeo " + ROMEO_VERSION + " is released for free distribution, and not for<br>"
      + "resale, under the permissions granted in the UltraCorps help<br>"
      + "pages in the section on \"Player-Created Programs\".<br>"

      + "<br>" + "Steve Jackson Games' permission to create and distribute such<br>"
      + "player created programs does not constitute an offer to provide<br>"
      + "support for Romeo by Steve Jackson Games. This software is not<br>"
      + "official and is not written by or endorsed by Steve Jackson Games.<br>"

      + "<br>" + "Steve Jackson Games: www.sjgames.com<br>" + "UltraCorps: www.ultracorps.com<br>"

      + "</center></html>";
  /*
   * Note: The URLs would be better as links. Currently if we make it an href
   * its rendered by JLabel as though it was a link, but Swing won't actually
   * bring up a browser if its clicked, so its better to just render it flat!
   */

  //Definitions used for the splash screen and its progressor
  //Need to modify this number if we add more stuff to the startup
  private static int NUMBER_OF_PROGRESS_ITEMS = 19;
  private static JProgressBar _progress;
  private static JFrame _splash;
  //...

  /**
   * The global reference to Romeo's window frame. This is retrieved via the
   * static getMainFrame() method.
   */
  private static MainFrame _mainFrame;

  public static void main(String[] args) {
    try {
      Romeo.showSplash();
      Romeo.checkUnitsFileExists();
      Romeo.incrementSplashProgress("In fair Verona, where we lay our scene");
      Romeo romeo = fromForthTheFatalLoins();
      romeo.whereforeArtThou();
    } catch(Throwable t) {
      Romeo.showStartupError(t);
      LoggerFactory.getLogger(Romeo.class).error("Romeo startup failure", t);
    }
  }
  
  /**
   * Initialise the database (create or ensure up-to-date) and create the Romeo object
   */
  private static Romeo fromForthTheFatalLoins() {
    QndDataSource dataSource = new QndDataSource();
    dataSource.setDriver("org.hsqldb.jdbcDriver");
    dataSource.setDatabase("jdbc:hsqldb:database/romeo");  //relative location of the database (to be created on first use)
    
    //Minimal bunch of objects we need to run all the db initialisers
    IKeyGen keyGen = new KeyGenImpl();    
    IUnitService unitService = new UnitServiceImpl(LoggerFactory.getLogger(UnitServiceImpl.class), dataSource, keyGen);
    ISettingsService settingsService = new SettingsServiceImpl(LoggerFactory.getLogger(ScenarioServiceImpl.class),dataSource, keyGen);
    IPlayerService playerService = new PlayerServiceImpl(LoggerFactory.getLogger(PlayerServiceImpl.class), dataSource, keyGen);
    IWorldService worldService = new WorldServiceImpl(LoggerFactory.getLogger(WorldServiceImpl.class), dataSource, keyGen, playerService, unitService, settingsService);
    List<String> unitColumns = Arrays.asList("name", "firepower", "maximum","offense", "defense", "attacks", "pd", "carry", "speed", "complexity", "basePrice", "cost", "license", "unitId", "turnAvailable", "stealth", "scanner");
    
    //Initialisers should be run as early as feasibly possible because until the database is in the correct state
    //code that tries to use it will fail, so we need to do this before anything does try.
    new DatabaseInitialiser(
        LoggerFactory.getLogger(DatabaseInitialiser.class),
        dataSource, 
        Arrays.asList(
          new HsqldbSettingsInitialiser(LoggerFactory.getLogger(HsqldbSettingsInitialiser.class)),
          new SettingsServiceInitialiser(LoggerFactory.getLogger(SettingsServiceInitialiser.class)),
          new WorldServiceInitialiser(
              LoggerFactory.getLogger(WorldServiceInitialiser.class),
              keyGen, 
              settingsService),
          new UnitServiceInitialiser(
              LoggerFactory.getLogger(UnitServiceInitialiser.class),
              new UnitImporterImpl(LoggerFactory.getLogger(UnitImporterImpl.class), unitService), 
              unitColumns, 
              new AdjustmentsFileReader()),
          new XFactorServiceInitialiser(
              LoggerFactory.getLogger(XFactorServiceInitialiser.class),
              unitService,
              new XFactorFileReader(), 
              keyGen),
          new PlayerServiceInitialiser(LoggerFactory.getLogger(PlayerServiceInitialiser.class), keyGen),
          new ScenarioServiceInitialiser(LoggerFactory.getLogger(ScenarioServiceInitialiser.class))))
    .runInitialisers();
    
    //Now that the db is ready we are safe to start constructing all the rest of the stuff
    IScenarioService scenarioService = new ScenarioServiceImpl(LoggerFactory.getLogger(ScenarioServiceImpl.class), dataSource, keyGen);
    IXFactorService xFactorService = new XFactorServiceImpl(LoggerFactory.getLogger(XFactorServiceImpl.class), dataSource, keyGen, unitService);
    IExpressionParser expressionParser = new ExpressionParserImpl();
    IXFactorCompiler xFactorCompiler = new XFactorCompilerImpl(expressionParser, xFactorService);
    RomeoFormInitialiser formInitialiser = new RomeoFormInitialiser(playerService, xFactorService, unitService, expressionParser);
    NavigatorPanel navigatorPanel = new NavigatorPanel(LoggerFactory.getLogger(NavigatorPanel.class));    
    IEventHub shutdownNotifier = new EventHubImpl(LoggerFactory.getLogger(EventHubImpl.class));    
    UnitFormFactory unitFormFactory = new UnitFormFactory(formInitialiser, unitService, xFactorService);
    PlayerFormFactory playerFormFactory = new PlayerFormFactory(formInitialiser, playerService, worldService, settingsService);
    WorldFormFactory worldFormFactory = new WorldFormFactory(formInitialiser, worldService, settingsService, playerService, playerFormFactory);
    XFactorFormFactory xFactorFormFactory = new XFactorFormFactory(formInitialiser, xFactorService);
    WorldImporterFactory worldImporterFactory = new WorldImporterFactory(worldService, playerService, settingsService);
        
    IMapLogic logic = new WorldMapLogic(
        LoggerFactory.getLogger(WorldMapLogic.class),
        worldService, 
        unitService, 
        settingsService, 
        playerService); 
    IRecordSelectionListener listener = new WorldNavigatorRecordSelectionListener(
        LoggerFactory.getLogger(WorldNavigatorRecordSelectionListener.class),
        navigatorPanel, 
        worldFormFactory, 
        worldService);
    GenericMap worldsMap = new GenericMap(
        LoggerFactory.getLogger(GenericMap.class),
        logic, 
        listener, 
        shutdownNotifier);
    worldsMap.setFont(new java.awt.Font("Arial", 0, 10));
    
    MapCenterer mapCenterer = new MapCenterer(
        LoggerFactory.getLogger(MapCenterer.class),
        settingsService, 
        worldService, 
        worldsMap);
    List<String> worldColumns = Arrays.asList("worldID", "name", "worldX", "worldY", "worldEi", "worldRer", "ownerID", "owner", "ownerRace", "class", "labour", "capital", "firepower", "team");
    BattleCalculatorFactory battleCalculatorFactory = new BattleCalculatorFactory(xFactorCompiler);
    
    MainFrameFactory mainFrameFactory = new MainFrameFactory(
        navigatorPanel, 
        worldsMap, 
        unitService, 
        settingsService, 
        playerService, 
        worldService, 
        shutdownNotifier, 
        worldColumns, 
        unitColumns, 
        scenarioService, 
        dataSource, 
        worldFormFactory, 
        unitFormFactory, 
        playerFormFactory, 
        xFactorFormFactory, 
        worldImporterFactory, 
        xFactorService, 
        xFactorCompiler, 
        battleCalculatorFactory);
    
    //add log thread listeners (use the service category for the logger)
    worldService.addListener(new LogThreadNameInvocationListener(LoggerFactory.getLogger(worldService.getClass())));
    unitService.addListener(new LogThreadNameInvocationListener(LoggerFactory.getLogger(unitService.getClass())));
    xFactorService.addListener(new LogThreadNameInvocationListener(LoggerFactory.getLogger(xFactorService.getClass())));
    playerService.addListener(new LogThreadNameInvocationListener(LoggerFactory.getLogger(playerService.getClass())));
    scenarioService.addListener(new LogThreadNameInvocationListener(LoggerFactory.getLogger(scenarioService.getClass())));
    settingsService.addListener(new LogThreadNameInvocationListener(LoggerFactory.getLogger(settingsService.getClass()))); 
    
    return new Romeo(
        dataSource,
        mapCenterer,
        mainFrameFactory);
  }
  
  /**
   * Fixes some 'issues' with the style of JFreeChart This code should be called
   * before any JFreeChart code is used
   */
  private static void fixJFreeChartStyle() {
    BarRenderer.setDefaultShadowsVisible(false);
    XYBarRenderer.setDefaultShadowsVisible(false);
    ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
  }

  //End of static definitions
  /////////////////////////////////////////////////////////////////////////////

  private final MapCenterer _mapCenterer;
  private final MainFrameFactory _fairVerona;

  public Romeo(
      DataSource dataSource, 
      MapCenterer mapCenterer,
      MainFrameFactory mainFrameFactory) {
    _mapCenterer = Objects.requireNonNull(mapCenterer, "mapCenterer may not be null");
    _fairVerona = Objects.requireNonNull(mainFrameFactory, "mainFrameFactory may not be null");
  }
  
  /**
   * Run the initialisers and then bring up the main UI by instantiating the MainFrame
   * (which needs to be done after the initialisers have all been run).
   */
  public void whereforeArtThou() {
    LoggerFactory.getLogger(Romeo.class).info("wherefore art thou Romeo?");
    final MainFrame frame = _fairVerona.layOurScene();
    Romeo.incrementSplashProgress("Open main frame");
    frame.setVisible(true);
    Romeo.setMainFrame(frame);
    Romeo.killSplash();
    
    //Now that the mainFrame is displayed the map centering should work properly
    //(it doesn't if we try to set it while setting up the ui as scrollPane reports its sizes as 0)
    SwingUtilities.invokeLater( _mapCenterer );
  }
  
  /**
   * Create and show the romeo splash screen. This includes a progress bar that
   * can be advanced with calls to incrementSplashProgress(). To hide the splash
   * screen it is necessary to call killSplash(). This call is made from the
   * MainFrame class.
   */
  protected static void showSplash() {
    try {
      _splash = new JFrame("loading Romeo...");
      _splash.setIconImage(GuiUtils.getImageIcon("/images/romeo.gif").getImage());
      JLabel label = new JLabel(GuiUtils.getImageIcon("/images/romeoSplash.gif"));

      _splash.getContentPane().setLayout(new BorderLayout());
      _splash.getContentPane().add(label, BorderLayout.CENTER);
      _splash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _progress = new JProgressBar(0, NUMBER_OF_PROGRESS_ITEMS); //TODO - figure out how to calc some of this
      _progress.setIndeterminate(false);
      _splash.getContentPane().add(_progress, BorderLayout.SOUTH);

      _splash.pack(); // size to fit contents
      _splash.setLocationRelativeTo(null); // Do after sizing
      _splash.setVisible(true);
    } catch(Exception e) {
      LoggerFactory.getLogger(Romeo.class).error("Error showing splash screen", e);
      killSplash();
    }
  }

  /**
   * Romeo 0.4.1 and beyond require that a standard unit.csv file be placed in the
   * resources folder to read the default unit definitions from. This method
   * simply checks for its existence and aborts if it is not there so that the
   * database is not created (if the database was created prior to aborting the
   * user would need to delete it manually before trying again).
   * @throws ApplicationException
   *           if the unit.csv file is not installed
   */
  static void checkUnitsFileExists() {
    if(!new ClassPathFile(UnitServiceInitialiser.UNITS_FILE_RESOURCE_PATH).exists()) {

      //prompt for an alternative filesystem path and then just copy that into resources folder for the
      //unit service initialiser to use later

      String welcomeText = "*** WELCOME TO ROMEO! *** \n\n"
          + "Before starting for the first time, Romeo requires you to supply a file containing unit information "
          + "whence the unit definitions may be imported.\n\n"
          + "You will need to download this file from the UltraCorps website as it is not permitted to distribute "
          + "a copy together with Romeo - and if one was included in the distribution it would soon become outdated anyway.\n\n"
          + NoUnitCsvFileException.TXT_WHERE_GOT_HELP
          + "\n\nHave you already downloaded the unit csv file? (Upon clicking yes, you will be prompted to "
          + " select it in a file chooser).\n\n";
      welcomeText = Convert.wordWrap(welcomeText, 80);
      int option = JOptionPane.showConfirmDialog(_mainFrame, welcomeText, "Unit CSV File Required",
          JOptionPane.YES_NO_OPTION);
      if(option == JOptionPane.YES_OPTION) {

        File importFolder = new File(System.getProperty("user.dir"));
        final JFileChooser chooser = new JFileChooser(importFolder);

        chooser.setDialogTitle("Select unit file");
        chooser.setFileHidingEnabled(false);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setApproveButtonText("Select");
        FileFilter fileFilter = new FileFilter() { //Filter to only show the .csv files
          @Override
          public boolean accept(File pathname) {
            if(pathname.isDirectory()) {
              return true;
            } else if(pathname.isFile()) {
              String filename = pathname.getName().toLowerCase();
              return filename.endsWith(".csv");
            }
            return false;
          }

          @Override
          public String getDescription() {
            return "CSV Files (Comma Seperated Values)";
          }
        };
        chooser.setFileFilter(fileFilter);
        int returnVal = chooser.showOpenDialog(_mainFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            URL contextLocation = ClassLoader.getSystemResource("context.xml");
            //Path resourcesFolder = Paths.get(contextLocation.toURI()).toFile().getParentFile().toPath();
            Path resourcesFolder = Paths.get(contextLocation.toURI()).getParent();
            Path unitFile = chooser.getSelectedFile().toPath();
            SettingsServiceInitialiser.__initialImportFolderPath = chooser.getSelectedFile().getParent();
            Path targetPath = resourcesFolder.resolve("unit.csv");
            //Copy the file to the resources folder where the unit service initialiser will pick it up later.
            //Supposedly Files.copy is synchronous (blocking), so why do we sometimes end up importing empty?
            Files.copy(unitFile, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
          } catch(Exception e) {
            throw new RuntimeException("Problem copying the units file to resources", e);
          }
        } else {
          //The user cancelled the file dialog
          showCsvRequiredError();
        }
      } else {
        //The user responded 'no'
        showCsvRequiredError();
      }
    }
  }
  
  /**
   * Show the 'sorry' message indicating the csv is required, and throw
   * a {@link NoUnitCsvFileException}
   * @throws NoUnitCsvFileException
   */
  private static void showCsvRequiredError() {
    String sorryText = "Yeah, sorry mate. Romeo's gonna need that file. If you're desperate you could just use an empty text file"
        + " (but then no unit definitions will be imported and you will have to create them manually via the UI).";
    sorryText = Convert.wordWrap(sorryText, 80); 
    JOptionPane.showMessageDialog(_mainFrame, sorryText, "NOROMEO4U Error", JOptionPane.ERROR_MESSAGE);
    throw new NoUnitCsvFileException(false);    
  }

  /**
   * Called in the event of an exception being caught while starting up Romeo
   * this will change the splash screen into a very rudimentary error message so
   * that the user at least knows something went wrong. (Its not going to be
   * winning any visual design awards thats for sure!)
   * @param e
   *          The exception that was caught - its message is included in the
   *          text
   */
  protected static void showStartupError(Throwable t) {
    if(_splash != null) {
      killSplash();
    }
    if(t instanceof NoUnitCsvFileException && !((NoUnitCsvFileException)t).isShowErrorDialog()) {
      ;
    } else {
      ErrorDialog dialog = new ErrorDialog("An error occured during startup!", t, true);
      dialog.show();
    }
  }

  /**
   * Increment the progress bar in the romeo splash screen. Nb: the total number
   * of tasks has been defined in the constant NUMBER_OF_PROGRESS_ITEMS.
   * @param task
   *          Name of whats being initialised
   */
  public static void incrementSplashProgress(final String task) {
    if(_progress == null)
      return;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        _progress.setValue(_progress.getValue() + 1);
        _progress.setString(task);
        _progress.setStringPainted(task != null);
      }
    });
  }

  /**
   * Hide and dispose of the romeo splash screen
   */
  public static void killSplash() {
    if(_splash != null) {
      _splash.setVisible(false);
      _splash.dispose();
      _splash = null;
    }
  }

  /**
   * Set the reference to the {@link MainFrame} instance
   * @param frame
   */
  private static void setMainFrame(MainFrame frame) {
    _mainFrame = frame;
  }

  /**
   * Returns the {@link MainFrame} instance. (An exception is thrown if it hasnt
   * been set)
   * @return mainFrame
   * @throws IllegalStateException
   *           if mainFrame reference not set yet
   */
  public static MainFrame getMainFrame() {
    if(_mainFrame == null) {
      throw new IllegalStateException("mainFrame reference not set");
    }
    return _mainFrame;
  }



}
