
package romeo;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.springframework.core.io.ClassPathResource;

import romeo.model.api.IServiceInitialiser;
import romeo.settings.impl.SettingsServiceInitialiser;
import romeo.ui.ErrorDialog;
import romeo.ui.MainFrame;
import romeo.ui.MapCenterer;
import romeo.units.impl.UnitServiceInitialiser;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.GuiUtils;

/**
 * Contains the main method that creates the ApplicationContext and starts the
 * application by invoking the Romeo object. The ApplicationContext is exposed
 * to classes in the application via the global CONTEXT.
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

      + "UltraCorps is copyright &copy; 1998-2002, 2005-2017 by<br>"
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

  /**
   * Global reference that allows access to the various services etc
   */
  public static RomeoContext CONTEXT = null;
  
  /**
   * Create the spring context, show the splash screen, obtain an instance of
   * Romeo and call its whereForArtThou() entry method. Note that the Romeo
   * instance is obtained from Spring to allow for dependency injection of the
   * initialisers etc.
   * @param args
   *          Currently unused
   */
  public static void main(String[] args) {
    try {
      Romeo.showSplash();
      Romeo.checkUnitsFileExists();
      Romeo.incrementSplashProgress("Initialise RomeoContext");
      Romeo.CONTEXT = new RomeoContext();
      Romeo romeo = Romeo.CONTEXT.createRomeo();
      romeo.whereforeArtThou();
    } catch(Exception e) {
      Romeo.showStartupError(e);
      Log log = LogFactory.getLog(Romeo.class);
      log.error("Romeo startup failure", e);
    }
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

  private List<IServiceInitialiser> _initialisers = Collections.emptyList();
  private DataSource _dataSource;
  private MapCenterer _mapCenterer;

  public Romeo(
      DataSource dataSource, 
      MapCenterer mapCenterer,
      List<IServiceInitialiser> initialisers) {
    _initialisers = Objects.requireNonNull(initialisers, "initialisers may not be null");
    _mapCenterer = Objects.requireNonNull(mapCenterer, "mapCenterer may not be null");
    _dataSource = Objects.requireNonNull(dataSource, "dataSource may not be null");
  }
  
  /**
   * Run the initialisers and create the UI by instantiating the MainFrame
   * instance
   */
  public void whereforeArtThou() {
    Log log = LogFactory.getLog(this.getClass());
    log.info("wherefore art thou Romeo?");
    runInitialisers();
    final MainFrame frame = Romeo.CONTEXT.createMainFrame();
    Romeo.incrementSplashProgress("Open main frame");
    frame.setVisible(true);
    Romeo.setMainFrame(frame);
    Romeo.killSplash();
    
    //Now that the mainFrame is displayed the map centering should work properly
    //(it doesn't if we try to set it while setting up the ui as scrollPane reports its sizes as 0)
    SwingUtilities.invokeLater( _mapCenterer );
    
  }
  
  /**
   * Runs the IServiceInitialisers defined in the initialisers property (which
   * we set using spring DI). The initialisers are responsible for examining the
   * state of the database and initialising it or updating schemas inherited
   * from older versions of Romeo.
   */
  protected void runInitialisers() {
    Log log = LogFactory.getLog(this.getClass());
    if(_dataSource == null) {
      throw new NullPointerException("dataSource not set");
    }
    try {
      Romeo.incrementSplashProgress("Start database");
      Connection connection = null;
      try {
        connection = _dataSource.getConnection();
      } catch(SQLException connEx) {
        String msg = connEx.getMessage();
        if("error in script file line: 4 unexpected token: TRIGGER".equalsIgnoreCase(msg)
            || msg.toLowerCase().contains("unexpected token: trigger")) { //I can fix it!
          log.info("Fixing invalid use of TRIGGER keyword in database created with an older Romeo and HSQLDB version");
          updateTriggerField();
          connection = _dataSource.getConnection(); //retry after fix
        } else { //Ralph wrecked it
          throw connEx;
        }
      }

      try {
        long startTime = System.currentTimeMillis();
        Set<String> tableNames = DbUtils.getTableNames(connection);
        log.info("Executing service initialisers");
        for(IServiceInitialiser initialiser : _initialisers) {
          Romeo.incrementSplashProgress("Run " + initialiser.getClass().getName());
          log.info("Executing service initialiser:" + initialiser);
          initialiser.init(tableNames, connection);
        }
        long endTime = System.currentTimeMillis();
        log.info("Executed service initialisers in " + (endTime-startTime) + " ms");
      } finally {
        connection.close();
      }
    } catch(ApplicationException ae) {
      throw ae;
    } catch(Exception e) {
      throw new RuntimeException("Exception caught running initialisers", e);
    }
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
      Log log = LogFactory.getLog(Romeo.class);
      log.error("Error showing splash screen", e);
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
    if(!new ClassPathResource(UnitServiceInitialiser.UNITS_FILE_RESOURCE_PATH).exists()) {

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
  protected static void showStartupError(Exception e) {
    if(_splash != null) {
      killSplash();
    }
    if(e instanceof NoUnitCsvFileException && !((NoUnitCsvFileException)e).isShowErrorDialog()) {
      ;
    } else {
      ErrorDialog dialog = new ErrorDialog("An error occured during startup!", e, true);
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

  /**
   * The older version of hsqldb used by Romeo 0.5.x allowed us to use the name
   * 'trigger' as a column name, however the current version does not and won't
   * even let us open the old databases. To fix this we need to go and tweak the
   * .script file itself! See: deployment-chapt.html#dec_script_manual_change in
   * the hsqldb documentation.
   */
  protected void updateTriggerField() {
    try {
      BufferedReader reader = null;
      BufferedWriter writer = null;
      try {
        File romeoScript = new File("database/romeo.script");
        File romeoScriptOld = new File("database/romeo.script.old");

        //First rename the old script file
        boolean ok = romeoScript.renameTo(romeoScriptOld);
        if(!ok) {
          throw new RuntimeException("Failed to rename old .script file");
        }

        //Open the old script file for reading line by line
        FileInputStream inStream = new FileInputStream(romeoScriptOld);
        InputStreamReader streamReader = new InputStreamReader(inStream);
        reader = new BufferedReader(streamReader);

        //Open the new script file to copy to line by line
        FileOutputStream outStream = new FileOutputStream(romeoScript);
        OutputStreamWriter streamWriter = new OutputStreamWriter(outStream);
        writer = new BufferedWriter(streamWriter);

        String line = null;
        while((line = reader.readLine()) != null) { //Iterate all the lines in the old script file and write them to the
                                                      //new one, fixing any occurences of the invalid "trigger" and "TRIGGER" in the
                                                    //line, to the new valid names "TRIGGER" and "XFTRIGGER" before writing it.
          String newLine = line;
          newLine = Convert.replace(newLine, "trigger", "xfTrigger");
          newLine = Convert.replace(newLine, "TRIGGER", "XFTRIGGER");
          writer.write(newLine);
          writer.write("\n");
        }
      } finally {
        if(reader != null) {
          reader.close();
        }
        if(writer != null) {
          writer.close();
        }
      }
    } catch(IOException iox) {
      throw new RuntimeException("Error trying to fix trigger column in database", iox);
    }

  }

}
