package romeo.ui;

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.ui.forms.RomeoForm;

/**
 * Implements {@link IRecordSelectionListener} to open a form to edit or view
 * the specified record in the {@link NavigatorPanel}.
 */
public abstract class AbstractNavigatorRecordSelectionListener implements IRecordSelectionListener {
  private NavigatorPanel _navigatorPanel;

  /**
   * Constructor
   * @param navigatorPanel
   * @param formName
   */
  public AbstractNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel) {
    Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    _navigatorPanel = navigatorPanel;
  }

  /**
   * Opens the record in the navigator panel
   * @param record
   */
  protected void openRecord(Object record) {
    try {
      RomeoForm form = newForm();
      form.initialise(record);
      _navigatorPanel.display(form);
    } catch(Exception e) {
      ErrorDialog dialog = new ErrorDialog("Internal Error", e, false);
      dialog.show();
      Log log = LogFactory.getLog(this.getClass());
      log.error("Unable to display form", e);
    }
  }

  @Override
  public void recordSelected(Object record) {
    openRecord(record);
  }

  protected abstract RomeoForm newForm();
}
