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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import romeo.importdata.IUnitImportReport;
import romeo.importdata.IUnitImporter;
import romeo.importdata.impl.CsvUnitFile;
import romeo.importdata.impl.UnitImporterImpl;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.ErrorDialog;
import romeo.units.api.IUnitService;
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
  private final IUnitService _unitService;
  private final JFrame _mainFrame;
  private final List<String> _unitColumns;
  
  public ImportUnitsAction(
      Logger log,
      JFrame mainFrame,
      ISettingsService settingsService,
      IUnitService unitService,
      List<String> unitColumns) {
    super(log);
    _mainFrame = Objects.requireNonNull(mainFrame, "mainFrame may not be null");
    _settingsService = Objects.requireNonNull(settingsService,"settingsService may not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _unitColumns = Objects.requireNonNull(unitColumns, "unitColumns may not be null");
    
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
      File file = chooser.getSelectedFile();
      importFolderPath = file.getParent();
      _settingsService.setString(ISettings.IMPORT_FOLDER, importFolderPath);
      String[] columns = _unitColumns.toArray(new String[]{});
      if(_log.isDebugEnabled()) {
        _log.debug("Preparing to import from file " + file.getName());
        _log.debug("CSV Columns=" + Convert.toCsv( Arrays.asList(columns) ));
      }
      String nameColumn = "name"; //we no longer support changing this via context
      CsvUnitFile unitFile = new CsvUnitFile(LoggerFactory.getLogger(CsvUnitFile.class), file, columns, nameColumn);
      
      IUnitImporter unitImporter = new UnitImporterImpl(LoggerFactory.getLogger(UnitImporterImpl.class),_unitService);
      IUnitImportReport report = null;
      try {
        Map<String, Map<String, String>> adjustments = null; //we only adjust at startup currently. Later may be an option?
        boolean updateExistingUnits = true;
        report = unitImporter.importData(unitFile, adjustments, updateExistingUnits);
        if(report.getException() != null) {
          throw report.getException();
        }
      } catch(Exception ex) {
        _log.error("Import Error:", ex);
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
