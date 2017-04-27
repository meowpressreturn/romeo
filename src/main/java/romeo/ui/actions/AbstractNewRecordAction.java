package romeo.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.Action;

import romeo.ui.AbstractRomeoAction;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.utils.GuiUtils;

/**
 * Abstract superclass for new record actions that load the navigator panel with
 * a blank form for editing a new record.
 */
public abstract class AbstractNewRecordAction extends AbstractRomeoAction {
  private NavigatorPanel _navigatorPanel;

  /**
   * Constructor
   * @param navigatorPanel
   */
  public AbstractNewRecordAction(NavigatorPanel navigatorPanel) {
    super();
    Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    _navigatorPanel = navigatorPanel;
  }

  public void setName(String name) {
    putValue(Action.NAME, name);
  }

  public void setDescription(String description) {
    putValue(Action.LONG_DESCRIPTION, description);
  }

  public void setIcon(String path) {
    putValue(Action.SMALL_ICON, GuiUtils.getImageIcon(path));
  }

  /**
   * Callback for when action is invoked. Will Load a blank form into the
   * navigator panel. Will present an error dialog if this causes an exception.
   * @param e
   */
  @Override
  protected void doActionPerformed(ActionEvent e) {
    RomeoForm form = newForm();
    Object prototype = newRecord();
    form.initialise(prototype);
    _navigatorPanel.display(form);
  }

  /**
   * Class must implement to provide an instance of the required form to be put
   * in the navigator panel.
   * @return form
   */
  protected abstract RomeoForm newForm();

  /**
   * Class must implement to provide a fresh instance of the record to provide
   * the form with default values.
   * @return
   */
  protected abstract Object newRecord();
}
