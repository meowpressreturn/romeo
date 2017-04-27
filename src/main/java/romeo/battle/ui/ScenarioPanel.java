package romeo.battle.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import romeo.scenarios.api.IScenario;
import romeo.scenarios.api.IScenarioService;
import romeo.utils.GuiUtils;

public class ScenarioPanel extends JPanel {

  protected static final ImageIcon DELETE_ICON = GuiUtils.getImageIcon("/images/deleteScenario.gif");

  public ScenarioPanel(final IScenarioService scenarioService, final BattleFleetsManager battleFleetsManager) {
    Objects.requireNonNull(scenarioService, "scenarioService may not be null");
    Objects.requireNonNull(battleFleetsManager, "battleFleetsManager may not be null");

    final ScenarioCombo scenarioCombo = new ScenarioCombo(scenarioService);
    final JButton saveButton = new JButton(new SaveScenarioAction(scenarioService, battleFleetsManager, scenarioCombo));
    saveButton.setText("");
    final JButton deleteButton = new JButton(DELETE_ICON);
    deleteButton.setEnabled(false);

    //Listener to enable/disable the remove button depending on whether a scenario is selected
    scenarioCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        deleteButton.setEnabled(!scenarioCombo.isNewScenarioSelected());
      }
    });

    deleteButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IScenario scenario = scenarioCombo.getScenario();
        if(scenario != null) {
          scenarioService.deleteScenario(scenario.getId());
        }

      }
    });

    //ScenarioCombo listener to load fleet contents
    scenarioCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IScenario scenario = ((ScenarioCombo) e.getSource()).getScenario();
        if(scenario != null) {
          List<String> values = scenario.getFleets();
          battleFleetsManager.setFleetsFromValues(values);
        }
      }
    });

    //Layout fields in container
    scenarioCombo.getPreferredSize().height = saveButton.getPreferredSize().height;

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = new Insets(1, 1, 0, 0);

    gbc.gridx = 0;
    gbc.weightx = 1; //The selector is horizontally stretchy
    add(scenarioCombo, gbc);

    gbc.gridx++;
    gbc.weightx = 0; //the save and remove buttons take minimum space required
    add(saveButton, gbc);

    gbc.gridx++;
    add(deleteButton, gbc);

    revalidate();
  }

}
