package romeo.battle.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import romeo.Romeo;
import romeo.battle.IBattleCalculator;
import romeo.scenarios.api.IScenarioService;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.ui.NavigatorPanel;
import romeo.units.api.IUnitService;
import romeo.utils.GuiUtils;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;

/**
 * Panel to hold the fields for number of battles, the execute button and the ui
 * for fleets (which is managed by {@link BattleFleetsManager}).
 */
public class BattlePanel extends JPanel {
  protected static final ImageIcon EXECUTE_IMAGE = GuiUtils.getImageIcon("/images/execute.gif");

  protected NavigatorPanel _navigator;
  protected BattleFleetsManager _fleets;
  protected ISettingsService _settingsService;

  public BattlePanel(IUnitService unitService,
                     ISettingsService settingsService,
                     IXFactorService xFactorService,
                     IXFactorCompiler compiler,
                     IScenarioService scenarioService) {
    Objects.requireNonNull(unitService, "unitService must not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    Objects.requireNonNull(xFactorService, "xFactorService must not be null");
    Objects.requireNonNull(compiler, "compiler must not be null");
    Objects.requireNonNull(scenarioService, "scenarioService must not be null");

    _fleets = new BattleFleetsManager(settingsService, unitService, xFactorService, compiler);

    JButton _fightButton = new JButton("Execute", EXECUTE_IMAGE);
    _fightButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fight();
      }
    });

    JComponent fleetsUi = _fleets.getComponent();

    ScenarioPanel scenarioPanel = new ScenarioPanel(scenarioService, _fleets);

    //Layout fields in container
    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    Insets insets = new Insets(2,8,2,8); //new Insets(1, 1, 0, 0);
    gbc.weightx = 1; //expand horizontally to fill free space

    gbc.gridx = 0;
    gbc.insets = new Insets(8,8,2,8); //special insets for the top thing
    add(scenarioPanel, gbc);
    gbc.gridy++;

    gbc.insets = insets;
    gbc.gridx = 0;
    add(_fightButton, gbc);
    gbc.gridy++;

    gbc.gridx = 0;
    add(fleetsUi, gbc);
    gbc.gridy++;

    //filler that pushes everything up
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 2;
    gbc.weighty = 2;
    //add(new JLabel(""), gbc);
    add( Box.createGlue(), gbc );
    revalidate();
  }

  protected void fight() {
    int numberOfBattles = (int) _settingsService.getLong(ISettings.NUMBER_OF_BATTLES);
    IBattleCalculator bc = Romeo.CONTEXT.createBattleCalculator();
    bc.setNumberOfBattles(numberOfBattles);
    _fleets.read(bc);
    BattleProgressorImpl progressor = new BattleProgressorImpl(Romeo.getMainFrame(), bc);
    progressor.setNavigator(_navigator);
    progressor.executeCalculator();
  }

  public NavigatorPanel getNavigator() {
    return _navigator;
  }

  public void setNavigator(NavigatorPanel panel) {
    _navigator = panel;
  }

}
