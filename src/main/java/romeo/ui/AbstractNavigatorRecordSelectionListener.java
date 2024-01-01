package romeo.ui;

import java.util.Objects;

import org.slf4j.Logger;

import romeo.ui.forms.RomeoForm;

/**
 * Implements {@link IRecordSelectionListener} to open a form to edit or view
 * the specified record in the {@link NavigatorPanel}.
 */
public abstract class AbstractNavigatorRecordSelectionListener implements IRecordSelectionListener {
  
  protected final Logger _log;
  
  private final NavigatorPanel _navigatorPanel;

  /**
   * Constructor
   * @param logger you should use the logger category for the concrete subclass
   * @param navigatorPanel
   * @param formName
   */
  public AbstractNavigatorRecordSelectionListener(Logger log, NavigatorPanel navigatorPanel) {
    _log = Objects.requireNonNull(log, "log may not be null");
    _navigatorPanel = Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
  }

  /**
   * Opens the record in the navigator panel
   * @param record
   */
  protected void openRecord(Object record) {
    try {
      RomeoForm form = newForm(record);
      _navigatorPanel.display(form);
    } catch(Exception e) {
      ErrorDialog dialog = new ErrorDialog("Internal Error", e, false);
      dialog.show();
      _log.error("Unable to display form", e);
    }
  }

  @Override
  public void recordSelected(Object record) {
    openRecord(record);
  }

  protected abstract RomeoForm newForm(Object record);
}
