package romeo.worlds.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import romeo.Romeo;
import romeo.importdata.IWorldImportReport;
import romeo.importdata.IWorldImporter;
import romeo.importdata.impl.CsvWorldFile;
import romeo.importdata.impl.WorldImporterFactory;
import romeo.ui.ErrorDialog;
import romeo.ui.NumericCellRenderer;
import romeo.ui.actions.ImportWorldsAction;
import romeo.utils.GuiUtils;

public class ImportWorldsHelper {
  
  
  /**
   * Create a Runnable to execute the imports and a ProgressMonitor to display
   * the progress of the task, and begin the task. Typically this would be
   * invoked by the {@link ImportWorldsAction}
   * @param columnNames
   * @param turnFiles
   */
  public static void importWorlds(String[] columnNames, Map<Integer, File> turnFiles, WorldImporterFactory worldImporterFactory) {
    ImportWorldsProgressor progressor = new ImportWorldsProgressor(turnFiles);
    ImportWorldsTask task = new ImportWorldsTask(
        LoggerFactory.getLogger(ImportWorldsTask.class),
        columnNames, 
        turnFiles, 
        progressor, 
        worldImporterFactory);
    progressor.executeTask(task);
  }

  /**
   * Wraps the maps of {@link IWorldImportReport} and File of a directory import
   * for the purpose of presenting the information in tabular form in a JTable.
   */
  private static class ImportResultsModel extends AbstractTableModel {
    private Map<Integer, IWorldImportReport> _reports;
    private Map<Integer, File> _turnFiles;
    private Map<Integer, Integer> _rowTurn = new HashMap<>(); //maps row index to turn number

    public ImportResultsModel(Map<Integer, IWorldImportReport> reports, Map<Integer, File> turnFiles) {
      _reports = Objects.requireNonNull(reports, "reports may not be null");
      _turnFiles = Objects.requireNonNull(turnFiles, "turnFiles may not be null");
      int row = 0;
      for(int turn : _reports.keySet()) {
        _rowTurn.put(row++, turn);
      }
    }

    @Override
    public int getRowCount() {
      return _rowTurn.size();
    }

    @Override
    public int getColumnCount() {
      return 6;
    }

    @Override
    public Class<?> getColumnClass(int column) {
      switch (column){
        case 0:
          return Integer.class;
        case 1:
          return String.class;
        case 2:
          return Integer.class;
        case 3:
          return Integer.class;
        case 4:
          return Integer.class;
        case 5:
          return String.class;
        default:
          return Object.class;
      }
    }

    @Override
    public String getColumnName(int column) {
      switch (column){
        case 0:
          return "Turn";
        case 1:
          return "File";
        case 2:
          return "Imported";
        case 3:
          return "Updated";
        case 4:
          return "Players";
        case 5:
          return "Status";
        default:
          return "" + column;
      }
    }

    @Override
    public Object getValueAt(int row, int column) {
      int turn = _rowTurn.get(row);
      if(column == 0) {
        return turn;
      }
      IWorldImportReport report = _reports.get(turn);
      if(report == null) {
        return " - ";
      } //Shouldnt occur now
      switch (column){
        case 1:
          return _turnFiles.get(turn).getName();
        case 2:
          return _reports.get(turn).getImportedWorldsCount();
        case 3:
          return _reports.get(turn).getUpdatedWorldsCount();
        case 4:
          return _reports.get(turn).getImportedPlayersCount(); //TODO present seperately
        case 5:
          return _reports.get(turn).getException() == null ? "OK" : "ERROR";
        default:
          return "?";
      }
    }

    /**
     * Return the exception from the IWorldReport associated with the specified
     * table row (nb: row = turn-1)
     * @param row
     * @return
     */
    public Exception getExceptionForRow(int row) {
      int turn = _rowTurn.get(row);
      IWorldImportReport report = _reports.get(turn);
      return (report == null) ? null : report.getException();
    }

    /**
     * Returns a count of the number of reports that have an exception in the
     * report
     * @return errorCount
     */
    public int getErrorCount() {
      int c = 0;
      for(IWorldImportReport report : _reports.values()) {
        if(report.getException() != null) {
          c++;
        }
      }
      return c;
    }

  }

  /**
   * Listener class to attach to the directory import results table's error
   * column that will bring up an ErrorDialog to show error messages.
   */
  private static class ErrorColumnListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      if(e.getClickCount() >= 2) {
        JTable table = (JTable) e.getSource();
        ImportResultsModel model = (ImportResultsModel) table.getModel();
        int row = table.getSelectedRow();
        Exception exception = model.getExceptionForRow(row);
        if(exception != null) {
          ErrorDialog d = new ErrorDialog("Import Exception for turn " + (row + 1), exception, false);
          String filename = "" + model.getValueAt(row, 1);
          d.setTitle("Error importing from '" + filename + "'");
          d.show();
        }
      }
    }
  }

  /**
   * Manages the display of the ProgressMonitor and provides the methods which
   * the task can call while it proceeds to inform the monitor of the progress.
   * Upon completion of the task it will generate and display a dialog box
   * showing the results.
   */
  private static class ImportWorldsProgressor {
    private ProgressMonitor _pm;
    private Map<Integer, IWorldImportReport> _results;
    private Map<Integer, File> _turnFiles;

    private ImportWorldsProgressor(Map<Integer, File> turnFiles) {
      _turnFiles = Objects.requireNonNull(turnFiles, "turnFiles may not be null");
    }

    public void executeTask(ImportWorldsTask task) {
      //Size of progress monitor dialog is based on the initial note so we make it wider by spacing it out
      _pm = new ProgressMonitor(Romeo.getMainFrame(), "Importing Map Files...",
          "Preparing to import maps from files............................", 0, _turnFiles.size());
      _pm.setMillisToDecideToPopup(0);
      _pm.setMillisToPopup(0);
      Thread t = new Thread(task);
      t.start();
    }

    public void complete(Map<Integer, IWorldImportReport> results) { //Close the monitor and show a dialog with the results
      _results = Objects.requireNonNull(results, "reports may not be null");
      SwingUtilities.invokeLater(new Runnable() { //changes to ui must occur in event dispatching thread
        @Override
        public void run() {
          _pm.close();
          showResultsDialog();
        }
      });
      System.gc();
    }

    /**
     * Show a dialog with the results. This needs to execute on the
     * EventDispatchThread. It is assumed that the importing thread is no longer
     * accessing this object, so access to instance variables is not
     * synchronized. (Called with an invokeLater by complete() method)
     */
    private void showResultsDialog() {
      //Now display results
      ImportResultsModel resultsModel = new ImportResultsModel(_results, _turnFiles);
      JTable resultsTable = new JTable(resultsModel);
      resultsTable.setDefaultRenderer(Integer.class, new NumericCellRenderer(0));
      GuiUtils.setColumnWidths(resultsTable, new int[] { 48, 192, 80, 80, 80, 80 });
      ErrorColumnListener ecListener = new ErrorColumnListener();
      resultsTable.addMouseListener(ecListener);

      boolean modal = false;
      final JDialog resultsDialog = new JDialog(Romeo.getMainFrame(), "Import Results", modal);
      Dimension size = (_results.size() < 4) ? new Dimension(560, 180) : new Dimension(560, 320);
      JScrollPane resultsScroll = new JScrollPane(resultsTable);
      resultsDialog.getContentPane().setLayout(new BorderLayout());
      resultsDialog.getContentPane().add(resultsScroll, BorderLayout.CENTER);
      resultsScroll.setSize(size);
      resultsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      resultsScroll.getVerticalScrollBar().setUnitIncrement(16);
      resultsDialog.setSize(size);

      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          resultsDialog.setVisible(false);
          resultsDialog.dispose();
        }
      });
      closeButton.setIcon(GuiUtils.getImageIcon("/images/cross.gif"));
      resultsDialog.getContentPane().add(closeButton, BorderLayout.SOUTH);

      Font font = new Font(Font.DIALOG, Font.BOLD, 14);
      int ec = resultsModel.getErrorCount();
      if(ec == 0) {
        int n = _results.size();
        JLabel okLabel = new JLabel(n == 1 ? "File Imported" : n + " files were imported");
        okLabel.setFont(font);
        okLabel.setHorizontalAlignment(JLabel.CENTER);
        resultsDialog.getContentPane().add(okLabel, BorderLayout.NORTH);
      } else {
        String plural = ec > 1 ? "s" : "";
        String msg = "<html>There were problems with " + ec + " file" + plural + ".<br>"
            + "Double-click the relevant row" + plural + " for details.</html>";
        JLabel msgLabel = new JLabel(msg);
        msgLabel.setOpaque(true); //needed to make the background color show up
        msgLabel.setBackground(Color.RED);
        msgLabel.setFont(font);
        msgLabel.setHorizontalAlignment(JLabel.CENTER);
        resultsDialog.getContentPane().add(msgLabel, BorderLayout.NORTH);
      }

      resultsDialog.setVisible(true);
      resultsDialog.setLocationRelativeTo(null); //center

      closeButton.requestFocusInWindow();
    }

    /**
     * Update the progress value and check if cancel was clicked
     * @param turn
     * @param filename
     * @return cancelled
     */
    public boolean updateProgress(int turn, String filename) {
      String note = "Importing turn " + turn + " from " + filename;
      return GuiUtils.updateProgressMonitor(_pm, turn, note);
    }
  }

  /**
   * A Runnable that will be executed in a new thread by the
   * {@link ImportWorldsProgressor} and that will read each file in turn,
   * updating the progressor with its progress and checking after each import to
   * see if it was cancelled or not. It will accumulate a map of results and
   * pass this to the progressor upon completion.
   */
  private static class ImportWorldsTask implements Runnable {
    private final ImportWorldsProgressor _progressor;
    private final Map<Integer, File> _turnFiles;
    private final String[] _columnNames;
    private final Logger _log;
    private final WorldImporterFactory _worldImporterFactory;

    private ImportWorldsTask(
        Logger log,
        String[] columnNames, 
        Map<Integer, File> turnFiles, 
        ImportWorldsProgressor progressor,
        WorldImporterFactory worldImporterFactory) {
      _log = Objects.requireNonNull(log, "log may not be null");
      _columnNames = Objects.requireNonNull(columnNames, "columnNames, may not be null");
      _turnFiles = Objects.requireNonNull(turnFiles, "turnFiles may not be null");
      _progressor = progressor; //optional
      _worldImporterFactory = Objects.requireNonNull(worldImporterFactory, "worldImporter may not be null");
    }

    /**
     * If a progressor was provided, update it with current progress and check
     * if the cancel button was clicked, in which case returns false indicating
     * not to continue
     * @param turn
     * @return continue
     */
    private boolean updateProgress(int turn) {
      if(_log.isDebugEnabled()) {
        _log.trace("updateProgress(" + turn + ")");
      }
      if(_progressor != null) {
        File file = _turnFiles.get(turn);
        boolean cancelled = _progressor.updateProgress(turn, (file == null ? "" : file.getName()));
        return(cancelled == false);
      }
      return true;
    }

    private void complete(Map<Integer, IWorldImportReport> reports) {
      if(_progressor != null) {
        _progressor.complete(reports);
      }
    }

    @Override
    public void run() {
      Map<Integer, IWorldImportReport> reports = new TreeMap<Integer, IWorldImportReport>();
      importloop: for(Map.Entry<Integer, File> turnFile : _turnFiles.entrySet()) {
        int turn = turnFile.getKey();
        if(!updateProgress(turn)) {
          break importloop;
        }
        File file = turnFile.getValue();
        _log.info("Preparing to import data for turn " + turn + " from " + turnFile.getValue().getName());
        IWorldImporter importer = _worldImporterFactory.newInstance(); //use a fresh importer for each file
        try {
          CsvWorldFile worldFile = new CsvWorldFile(LoggerFactory.getLogger(CsvWorldFile.class), file, _columnNames, "name");
          IWorldImportReport report = importer.importData(worldFile, turn);
          reports.put(turn, report);
        } catch(final Exception ex) {
          _log.error("Fatal Map Import Error:", ex);
          SwingUtilities.invokeLater(new Runnable() {            
            @Override
            public void run() {
              ErrorDialog dialog = new ErrorDialog("Map Import Error",ex,false);
              dialog.show();
            }
          });     
        }
      } //importloop
      complete(reports);
    }
  }

  private ImportWorldsHelper() {
  } //This helper class only presents a static method to use it
}
