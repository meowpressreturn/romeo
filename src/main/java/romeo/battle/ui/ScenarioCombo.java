package romeo.battle.ui;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import romeo.scenarios.api.IScenario;
import romeo.scenarios.api.IScenarioService;
import romeo.scenarios.api.ScenarioId;
import romeo.ui.forms.AbstractRecordCombo;
import romeo.utils.GuiUtils;

public class ScenarioCombo extends AbstractRecordCombo implements ListCellRenderer<Object> {

  private static final ImageIcon PLUS_ICON = GuiUtils.getImageIcon("/images/plus.gif");

  public static final String NEW_SCENARIO_TEXT = "<<New scenario>>";

  private DefaultListCellRenderer _renderer = new DefaultListCellRenderer();

  public ScenarioCombo(IScenarioService scenarioService) {
    super(scenarioService, NEW_SCENARIO_TEXT);
    setRenderer(this);
  }

  @Override
  protected List<IScenario> loadRecords() {
    return ((IScenarioService) _service).getScenarios();
  }

  public boolean isNewScenarioSelected() {
    return NEW_SCENARIO_TEXT.equals(getSelectedItem());
  }

  public IScenario getScenario() {
    return (IScenario) getSelectedRecord();
  }

  public void setScenario(IScenario scenario) {
    setSelectedRecord(scenario);
  }

  public void setScenarioById(ScenarioId id) {
    if(id == null) {
      setSelectedRecord(null);
    } else {
      IScenario scenario = ((IScenarioService) _service).loadScenario(id);
      setScenario(scenario);
    }
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {

    String display = (value instanceof IScenario) ? ((IScenario) value).getName() : "" + value;
    JLabel label = (JLabel) _renderer.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);

    if(NEW_SCENARIO_TEXT.equals(value)) {
      label.setIcon(PLUS_ICON);
    }
    return label;
  }

}
