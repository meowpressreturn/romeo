package romeo.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.importdata.impl.WorldImporterFactory;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.CsvFileFilter;
import romeo.ui.DirectoryClickListener;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
import romeo.worlds.api.IWorldService;
import romeo.worlds.ui.ImportWorldsHelper;

public class ImportWorldsAction extends AbstractRomeoAction {

  private final ISettingsService _settingsService;
  private final IWorldService _worldService;
  private final String[] _columns;
  private final JFrame _mainFrame;
  private final WorldImporterFactory _worldImporterFactory;
  
  /**
   * Constructor
   * @param mainFrame
   * @param settingsService
   * @param worldService
   * @param columns
   */
  public ImportWorldsAction(
      JFrame mainFrame,
      ISettingsService settingsService,
      IWorldService worldService,
      String[] columns,
      WorldImporterFactory worldImporterFactory) {
    _mainFrame = Objects.requireNonNull(mainFrame, "mainFrame may not be null");
    _settingsService = Objects.requireNonNull(settingsService,"settingsService may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _columns = Objects.requireNonNull(columns, "columns may not be null");
    _worldImporterFactory = Objects.requireNonNull(worldImporterFactory, "worldImporterFactory");
        
    putValue(Action.LONG_DESCRIPTION, "Update Map data from file");
    putValue(Action.NAME, "Import Map Data");
    putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/importCsv.gif"));
  }

  /**
   * Action callback method. This will perform the work of providing a dialog to
   * choose the file, invoking the importer (found in the context under the name
   * "worldImporter") and displaying a message box to show the results.
   * @param event
   */
  @Override
  public void doActionPerformed(ActionEvent event) {
    int currentTurn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);

    String importFolderPath = _settingsService.getString(ISettings.IMPORT_FOLDER);
    if(importFolderPath.isEmpty()) {
      importFolderPath = System.getProperty("user.dir");
    }
    File importFolder = new File(importFolderPath);
    JFileChooser chooser = new JFileChooser(importFolder);
    chooser.setDialogTitle("Select World data to import");
    chooser.setFileHidingEnabled(false);
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.setApproveButtonText("Import");
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.addMouseListener(new DirectoryClickListener(chooser)); //allow doubleclick drilldown
    chooser.setFileFilter(new CsvFileFilter());
    int returnVal = chooser.showOpenDialog(_mainFrame);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if(file != null) {
        importFolderPath = file.getParent();
        _settingsService.setString(ISettings.IMPORT_FOLDER, importFolderPath); //Remember last opened directory

        if(file.isDirectory()) { //Multiple file import
          importDirectory(file);
        } else { //Single File Import          
          importSingleFile(file, currentTurn);
        }
      }
    }

  }

  /**
   * Returns a Map of the import files keyed by their turn , as extracted from
   * the filename, which should match the pattern "^.*worlds.*\\d+.csv$"
   * (example: worlds_01.csv, worlds_02.csv etc...) If no import files were
   * found then an emoty map is returned (never null). nb: treats all filenames
   * as lowercase The map returned will allow for ordered iteration of the
   * values (eg: it may be a treemap or a linked map of some kind.
   * @param directory
   *          Directory to search (non-recursively) for worlds csv files
   * @return map with Integer turn number as the key to a File for the relevant
   *         csv
   */
  private Map<Integer, File> findFiles(File directory) {
    Objects.requireNonNull(directory, "directory may not be null");
    if(!directory.isDirectory()) {
      throw new IllegalArgumentException("Not a directory");
    }
    File[] mapFiles = directory.listFiles();
    Map<Integer, File> turnFiles = new TreeMap<Integer, File>();
    if(mapFiles.length > 0) {
      Pattern namePattern = Pattern.compile("^.*worlds.*\\d+.csv$");
      Pattern numberPattern = Pattern.compile("\\d+");
      for(File file : mapFiles) {
        if(file.isFile()) {
          String name = file.getName().toLowerCase(Locale.US);
          if(namePattern.matcher(name).matches()) {
            //We want to extract the number from the rest of the filename. Anything after the number though
            //(apart from .csv) means the file is to be ignored (eg 13bad). Anything before the number is stripped.
            //The number may or may not have 0 prefixing (eg: 01, 007, etc).
            Matcher matcher = numberPattern.matcher(name);
            String turn = null;
            while(matcher.find()) {
              turn = matcher.group();
            }
            ; //get last number group
            turn = StringUtils.removeStart(turn, "0"); //exterminate leading 0s
            if(!("0".equals(turn) || turn == null || turn.isEmpty())) {
              turnFiles.put(Convert.toInt(turn), file);
            }
          }
        }
      }
    }
    return turnFiles;
  }

  private void importDirectory(File directory) {
    Log log = LogFactory.getLog(this.getClass());
    log.debug("Preparing to import from directory " + directory.getName());
    Map<Integer, File> turnFiles = findFiles(directory);
    int fileCount = turnFiles.size();
    if(fileCount == 0) {
      JOptionPane.showMessageDialog(_mainFrame,
          "No files were found to import in '" + directory.getAbsolutePath() + "'\n"
              + "File names should start with 'worlds' , must contain turn\n"
              + "numbers and end with '.csv' - for example: 'worlds_23.csv'",
          "Map import from folder '" + directory.getName() + "'", JOptionPane.WARNING_MESSAGE);
      return;
    }

    boolean anyHistory = false;
    historyCheck: for(int turn : turnFiles.keySet()) { //Do any of the turns we are importing for have any history already in the db?
      if(_worldService.haveData(turn)) {
        anyHistory = true;
        break historyCheck;
      }
    }
    if(anyHistory) {
      String message = "History already exists that may be overwritten.\n" + " Continue with import?\n";
      int ok = JOptionPane.showConfirmDialog(_mainFrame, message, "Import Map Data",
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if(ok != JOptionPane.YES_OPTION) { //Exit now if they dont want to continue
        return;
      }
    }
    //The helper will now run the import task simultaneously on another thread, show a progressmonitor
    //and when complete generate a dialog to show results.
    ImportWorldsHelper.importWorlds(_columns, turnFiles, _worldImporterFactory);
  }

  private void importSingleFile(File file, int turn) {
    Objects.requireNonNull(file, "file may not be null");
    if(_worldService.haveData(turn)) {
      String message = "History already exists for turn " + turn + " and may be overwritten.\n"
          + " Continue with import?\n";
      int ok = JOptionPane.showConfirmDialog(_mainFrame, message, "Import Map Data",
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if(ok != JOptionPane.YES_OPTION) { //Exit now if they dont want to continue
        return;
      }
    }

    Map<Integer, File> turnFiles = new TreeMap<>();
    turnFiles.put(turn, file);
    ImportWorldsHelper.importWorlds(_columns, turnFiles, _worldImporterFactory);
  }

}
