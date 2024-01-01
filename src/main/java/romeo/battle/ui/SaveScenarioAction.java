package romeo.battle.ui;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.slf4j.Logger;

import romeo.Romeo;
import romeo.scenarios.api.IScenario;
import romeo.scenarios.api.IScenarioService;
import romeo.scenarios.impl.ScenarioImpl;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.MainFrame;
import romeo.utils.GuiUtils;

public class SaveScenarioAction extends AbstractRomeoAction {

  protected static final ImageIcon SAVE_ICON = GuiUtils.getImageIcon("/images/saveScenario.gif");

  private IScenarioService _scenarioService;
  private BattleFleetsManager _battleFleetsManager;
  private ScenarioCombo _scenarioCombo;

  public SaveScenarioAction(
    Logger log,
    IScenarioService scenarioService,
    BattleFleetsManager battleFleetsManager,
    ScenarioCombo scenarioCombo) {
    super(log);
    _scenarioService = Objects.requireNonNull(scenarioService, "scenarioService may not be null");
    _battleFleetsManager = Objects.requireNonNull(battleFleetsManager, "battleFleetsManager may not be null");
    _scenarioCombo = Objects.requireNonNull(scenarioCombo, "scenarioCombo may not be null");
    putValue(Action.NAME, "Save Scenario");
    putValue(Action.LONG_DESCRIPTION, "Save simulator fleet contents as a scenario");
    putValue(Action.SMALL_ICON, SAVE_ICON);
  }

  @Override
  protected void doActionPerformed(ActionEvent e) {
    List<String> values = _battleFleetsManager.getFleetValues();
    IScenario selectedScenario = _scenarioCombo.getScenario();
    if(selectedScenario == null) {
      //Need to create a new scenario
      MainFrame mainFrame = Romeo.getMainFrame();
      String name = JOptionPane.showInputDialog(mainFrame, "Enter a name for scenario", "Create Scenario",
          JOptionPane.QUESTION_MESSAGE);
      if(name != null) {
        name = name.trim();
        if(name.isEmpty() || name.length() > IScenario.MAX_NAME_LENGTH) {
          JOptionPane.showMessageDialog(mainFrame,
              "Scenario name may not be empty or longer than " + IScenario.MAX_NAME_LENGTH + " characters",
              "Invalid Scenario Name", JOptionPane.ERROR_MESSAGE);
        } else {
          //Ok, we have a valid name we can save with
          //TODO - should we block duplicate names?
          //TODO - block the 'New scenario' text from being a name
          IScenario scenario = new ScenarioImpl(null, name, values);
          scenario = _scenarioService.saveScenario(scenario);
          _scenarioCombo.setScenario(scenario);
        }
      }

    } else {
      //Saving over an existing scenario
      IScenario scenario = new ScenarioImpl(selectedScenario.getId(), selectedScenario.getName(), values);
      _scenarioService.saveScenario(scenario);
    }

  }

}
