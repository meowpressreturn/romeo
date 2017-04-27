package romeo.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.JPanel;

import romeo.Romeo;
import romeo.settings.ui.PreferencesControls;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.NavigatorPanel;
import romeo.utils.GuiUtils;

public class OpenPreferencesAction extends AbstractRomeoAction {
  private NavigatorPanel _navigatorPanel;

  /**
   * Constructor
   * @param navigatorPanel
   */
  public OpenPreferencesAction(NavigatorPanel navigatorPanel) {
    super();
    Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    _navigatorPanel = navigatorPanel;
    putValue(Action.NAME, "Options");
    putValue(Action.LONG_DESCRIPTION, "Manage Romeo Options");
    putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/prefs.gif"));
  }

  /**
   * Callback for when action is invoked. Will Load a blank form into the
   * navigator panel. Will present an error dialog if this causes an exception.
   * @param e
   */
  @Override
  protected void doActionPerformed(ActionEvent e) {
    PreferencesControls prefsCtrl = (PreferencesControls) Romeo.CONTEXT.createPreferencesControls();
    JPanel prefsPanel = prefsCtrl.getPanel();
    _navigatorPanel.display(prefsPanel);
  }
}
