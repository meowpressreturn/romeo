package romeo.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.RomeoContext;
import romeo.importdata.IUnitImportReport;
import romeo.importdata.IUnitImporter;
import romeo.importdata.impl.CsvUnitFile;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.ErrorDialog;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

/**
 * The UI Action for an import operation. This will present a file dialog to
 * allow the user to specify the unit csv file to be imported and then invoke
 * the unitImporter (as defined in the Spring context) to import it. A message
 * box showing the result of the operation will also be displayed.
 */
public class ImportUnitsAction extends AbstractRomeoAction {

  private final ISettingsService _settingsService;
  private final JFrame _mainFrame;
  private final RomeoContext _context;
  private final List<String> _unitColumns;
  
  public ImportUnitsAction(JFrame mainFrame,
      ISettingsService settingsService,
      List<String> unitColumns,
      RomeoContext context) {
    _mainFrame = Objects.requireNonNull(mainFrame, "mainFrame may not be null");
    _settingsService = Objects.requireNonNull(settingsService,"settingsService may not be null");
    _unitColumns = Objects.requireNonNull(unitColumns, "unitColumns may not be null");
    _context = Objects.requireNonNull(context,"context may not be null");
    
    putValue(Action.LONG_DESCRIPTION, "Update Unit data from file");
    putValue(Action.NAME, "Import Unit Data");
    putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/importCsv.gif"));
  }

  /**
   * Action callback method. This will perform the work of providing a dialog to
   * choose the file, invoking the importer (found in the context under the name
   * "unitImporter") and displaying a message box to show the results.
   * @param event
   */
  @Override
  public void doActionPerformed(ActionEvent event) {
    String importFolderPath = _settingsService.getString(ISettings.IMPORT_FOLDER);
    if(importFolderPath.isEmpty()) {
      importFolderPath = System.getProperty("user.dir");
    }
    File importFolder = new File(importFolderPath);
    final JFileChooser chooser = new JFileChooser(importFolder);
    chooser.setDialogTitle("Select Unit data to import");
    chooser.setFileHidingEnabled(false);
    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    chooser.setApproveButtonText("Import");
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
      Log log = LogFactory.getLog(this.getClass());
      File file = chooser.getSelectedFile();
      importFolderPath = file.getParent();
      _settingsService.setString(ISettings.IMPORT_FOLDER, importFolderPath);
      String[] columns = _unitColumns.toArray(new String[]{});
      if(log.isDebugEnabled()) {
        log.debug("Preparing to import from file " + file.getName());
        log.debug("CSV Columns=" + Convert.toCsv( Arrays.asList(columns) ));
      }
      String nameColumn = "name"; //we no longer support changing this via context
      CsvUnitFile unitFile = new CsvUnitFile(file, columns, nameColumn);
      
      IUnitImporter unitImporter = _context.createUnitImporter();
      IUnitImportReport report = null;
      try {
        Map<String, Map<String, String>> adjustments = null; //we only adjust at startup currently. Later may be an option?
        boolean updateExistingUnits = true;
        report = unitImporter.importData(unitFile, adjustments, updateExistingUnits);
        if(report.getException() != null) {
          throw report.getException();
        }
      } catch(Exception ex) {
        log.error("Import Error:", ex);
        ErrorDialog dialog = new ErrorDialog("Unit Import Error", ex, false);
        dialog.show();
      }
      if(report != null) {
        String reportStr = "New Units: " + report.getImportedUnitsCount() + "\nUpdated: " + report.getUpdatedUnitsCount();
        if(report.getException() != null) {
          reportStr += "\nERROR:" + report.getException().getMessage();
        }
        JOptionPane.showMessageDialog(_mainFrame, reportStr, "Import from " + file.getName(),
            JOptionPane.INFORMATION_MESSAGE);
      }
    }

  }
}
