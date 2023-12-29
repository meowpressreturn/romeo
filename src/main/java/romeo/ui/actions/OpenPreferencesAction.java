package romeo.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.Action;

import romeo.scenarios.api.IScenarioService;
import romeo.settings.api.ISettingsService;
import romeo.settings.ui.PreferencesControls;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.NavigatorPanel;
import romeo.utils.GuiUtils;

public class OpenPreferencesAction extends AbstractRomeoAction {
  private final NavigatorPanel _navigatorPanel;
  private final ISettingsService _settingsService;
  private final IScenarioService _scenarioService;

  /**
   * Constructor
   * @param navigatorPanel
   */
  public OpenPreferencesAction(
      final NavigatorPanel navigatorPanel,
      final ISettingsService settingsService,
      final IScenarioService scenarioService) {
    super();
    _navigatorPanel = Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    _scenarioService= Objects.requireNonNull(scenarioService, "scenarioService must not be null");
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
    _navigatorPanel.display(new PreferencesControls(_settingsService, _scenarioService).getPanel());
  }
}
