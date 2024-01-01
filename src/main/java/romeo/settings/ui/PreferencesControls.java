package romeo.settings.ui;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;

import romeo.Romeo;
import romeo.model.api.IServiceListener;
import romeo.scenarios.api.IScenarioService;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.ui.CommonTextActionsMenu;
import romeo.ui.NamedPanel;
import romeo.ui.forms.IValidatingField;
import romeo.ui.forms.RNumericField;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

public class PreferencesControls implements IServiceListener, DocumentListener, ItemListener, ActionListener {
  
  protected NamedPanel _panel;
  protected ISettingsService _settingsService;

  protected RNumericField _numberOfBattlesField;
  protected JCheckBox _showRawFpField;

  protected RNumericField _fClassWorldField;
  protected RNumericField _eClassWorldField;
  protected RNumericField _dClassWorldField;
  protected RNumericField _cClassWorldField;
  protected RNumericField _bClassWorldField;
  protected RNumericField _aClassWorldField;
  protected JLabel _sClassWorldLabel;
  protected RNumericField _defcon5Field;
  protected RNumericField _defcon4Field;
  protected RNumericField _defcon3Field;
  protected RNumericField _defcon2Field;
  protected JLabel _defcon1Label;

  protected RNumericField _defaultScanner;

  protected JButton _saveButton;
  protected JButton _cancelButton;

  protected boolean _dirty;

  protected List<IValidatingField> _fields;
  
  private final Logger _log;

  public PreferencesControls(
      final Logger log,
      final ISettingsService settingsService, 
      final IScenarioService scenarioService) {
    _log = Objects.requireNonNull(log, "log may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");;
    settingsService.addListener(this);

    _fields = new ArrayList<IValidatingField>();

    _panel = new NamedPanel("Manage Options");
    GridBagConstraints pgbc = GuiUtils.prepGridBag(_panel);

    //Simulator Preferences
    JPanel simulatorPrefs = new JPanel();
    simulatorPrefs.setBorder(new TitledBorder("Simulator"));
    _numberOfBattlesField = new RNumericField();
    _numberOfBattlesField.setComponentPopupMenu(new CommonTextActionsMenu(_numberOfBattlesField));
    _numberOfBattlesField.getDocument().addDocumentListener(this);
    _fields.add(_numberOfBattlesField);

    _showRawFpField = new JCheckBox("Show raw firepower");
    _showRawFpField.addActionListener(this);

    JButton deleteAllScenariosButton = new JButton("Clear all scenarios");
    deleteAllScenariosButton.setIcon(GuiUtils.getImageIcon("/images/deleteScenario.gif"));
    deleteAllScenariosButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int choice = JOptionPane.showConfirmDialog(Romeo.getMainFrame(),
            "This will erase all the recorded scenarios immediately.\nProceed?", "Delete scenarios?",
            JOptionPane.OK_CANCEL_OPTION);
        if(choice == JOptionPane.OK_OPTION) {
          scenarioService.deleteAllScenarios();
        }
      }
    });

    GridBagConstraints gbc = GuiUtils.prepGridBag(simulatorPrefs);
    simulatorPrefs.add(new JLabel("Battles to run:"), gbc);
    gbc.gridx++;
    simulatorPrefs.add(_numberOfBattlesField, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    simulatorPrefs.add(_showRawFpField, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    simulatorPrefs.add(deleteAllScenariosButton, gbc);

    gbc.gridy++;
    gbc.gridx = 0;

    //Create a component to push the fields into place
    gbc.gridx = 2;
    gbc.weightx = 2;
    gbc.weighty = 2;
    simulatorPrefs.add(new JLabel(""), gbc);

    _panel.add(simulatorPrefs, pgbc);

    //Map preferences
    JPanel mapPrefs = new JPanel();
    //mapPrefs.setBackground(Color.CYAN);
    mapPrefs.setBorder(new TitledBorder("Map"));
    GridBagConstraints mgbc = GuiUtils.prepGridBag(mapPrefs);

    _fClassWorldField = new RNumericField();
    _eClassWorldField = new RNumericField();
    _dClassWorldField = new RNumericField();
    _cClassWorldField = new RNumericField();
    _bClassWorldField = new RNumericField();
    _aClassWorldField = new RNumericField();
    _sClassWorldLabel = new JLabel();

    _fClassWorldField.getDocument().addDocumentListener(this);
    _eClassWorldField.getDocument().addDocumentListener(this);
    _dClassWorldField.getDocument().addDocumentListener(this);
    _cClassWorldField.getDocument().addDocumentListener(this);
    _bClassWorldField.getDocument().addDocumentListener(this);
    _aClassWorldField.getDocument().addDocumentListener(this);

    _fClassWorldField.setComponentPopupMenu(new CommonTextActionsMenu(_fClassWorldField));
    _eClassWorldField.setComponentPopupMenu(new CommonTextActionsMenu(_eClassWorldField));
    _dClassWorldField.setComponentPopupMenu(new CommonTextActionsMenu(_dClassWorldField));
    _cClassWorldField.setComponentPopupMenu(new CommonTextActionsMenu(_cClassWorldField));
    _bClassWorldField.setComponentPopupMenu(new CommonTextActionsMenu(_bClassWorldField));
    _aClassWorldField.setComponentPopupMenu(new CommonTextActionsMenu(_aClassWorldField));
    
    _fields.add(_fClassWorldField);
    _fields.add(_eClassWorldField);
    _fields.add(_dClassWorldField);
    _fields.add(_cClassWorldField);
    _fields.add(_bClassWorldField);
    _fields.add(_aClassWorldField);

    JPanel worldSizePrefs = new JPanel();
    worldSizePrefs.setBorder(new TitledBorder("Size Classes"));
    gbc = GuiUtils.prepGridBag(worldSizePrefs);

    worldSizePrefs.add(new JLabel("F Class <"), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_fClassWorldField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldSizePrefs.add(new JLabel("E Class <"), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_eClassWorldField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldSizePrefs.add(new JLabel("D Class <"), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_dClassWorldField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldSizePrefs.add(new JLabel("C Class <"), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_cClassWorldField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldSizePrefs.add(new JLabel("B Class <"), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_bClassWorldField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldSizePrefs.add(new JLabel("A Class <"), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_aClassWorldField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldSizePrefs.add(new JLabel("S Class >="), gbc);
    gbc.gridx++;
    worldSizePrefs.add(_sClassWorldLabel, gbc);

    gbc.gridy++;
    gbc.gridx = 2;
    gbc.weightx = 2;
    gbc.weighty = 2;
    worldSizePrefs.add(new JLabel(""), gbc);

    mgbc.gridx = 0;
    mgbc.gridwidth = 2;
    mapPrefs.add(worldSizePrefs, mgbc);

    JPanel worldDefensePrefs = new JPanel();
    worldDefensePrefs.setBorder(new TitledBorder("Defense Classes"));
    gbc = GuiUtils.prepGridBag(worldDefensePrefs);

    _defcon5Field = new RNumericField();
    _defcon4Field = new RNumericField();
    _defcon3Field = new RNumericField();
    _defcon2Field = new RNumericField();
    _defcon1Label = new JLabel();

    _defcon5Field.getDocument().addDocumentListener(this);
    _defcon4Field.getDocument().addDocumentListener(this);
    _defcon3Field.getDocument().addDocumentListener(this);
    _defcon2Field.getDocument().addDocumentListener(this);
    
    _defcon5Field.setComponentPopupMenu(new CommonTextActionsMenu(_defcon5Field));
    _defcon4Field.setComponentPopupMenu(new CommonTextActionsMenu(_defcon4Field));
    _defcon3Field.setComponentPopupMenu(new CommonTextActionsMenu(_defcon3Field));
    _defcon2Field.setComponentPopupMenu(new CommonTextActionsMenu(_defcon2Field));

    _fields.add(_defcon5Field);
    _fields.add(_defcon4Field);
    _fields.add(_defcon3Field);
    _fields.add(_defcon2Field);

    worldDefensePrefs.add(new JLabel("DefCon 5 <"), gbc);
    gbc.gridx++;
    worldDefensePrefs.add(_defcon5Field, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldDefensePrefs.add(new JLabel("DefCon 4 <"), gbc);
    gbc.gridx++;
    worldDefensePrefs.add(_defcon4Field, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldDefensePrefs.add(new JLabel("DefCon 3 <"), gbc);
    gbc.gridx++;
    worldDefensePrefs.add(_defcon3Field, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldDefensePrefs.add(new JLabel("DefCon 2 <"), gbc);
    gbc.gridx++;
    worldDefensePrefs.add(_defcon2Field, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    worldDefensePrefs.add(new JLabel("DefCon 1 >="), gbc);
    gbc.gridx++;
    worldDefensePrefs.add(_defcon1Label, gbc);

    gbc.gridy++;
    gbc.gridx = 2;
    gbc.weightx = 2;
    gbc.weighty = 2;
    gbc.fill = GridBagConstraints.BOTH;
    worldDefensePrefs.add(new JLabel(""), gbc);

    mgbc.gridx = 2;
    mgbc.gridwidth = 2;
    mgbc.anchor = GridBagConstraints.NORTH;
    mapPrefs.add(worldDefensePrefs, mgbc);

    _defaultScanner = new RNumericField();
    _defaultScanner.setComponentPopupMenu(new CommonTextActionsMenu(_defaultScanner));
    _defaultScanner.getDocument().addDocumentListener(this);
    _fields.add(_defaultScanner);

    mgbc.gridy++;
    mgbc.gridx = 0;
    mgbc.gridwidth = 1;
    mapPrefs.add(new JLabel("Visual Range:"), mgbc);
    mgbc.gridx++;
    mapPrefs.add(_defaultScanner, mgbc);

    mgbc.gridy++;
    mgbc.gridx = 2;
    mgbc.weightx = 2;
    mgbc.weighty = 2;
    mgbc.fill = GridBagConstraints.BOTH;
    mapPrefs.add(new JLabel(""), mgbc);

    pgbc.gridy++;
    _panel.add(mapPrefs, pgbc);

    JPanel buttonPanel = new JPanel();
    gbc = GuiUtils.prepGridBag(buttonPanel);

    _saveButton = new JButton("Update");
    _saveButton.setIcon(GuiUtils.getImageIcon("/images/tick.gif"));
    _saveButton.addActionListener(this);
    _saveButton.setEnabled(false);
    buttonPanel.add(_saveButton, gbc);

    gbc.gridx++;
    _cancelButton = new JButton("Cancel");
    _cancelButton.setIcon(GuiUtils.getImageIcon("/images/cross.gif"));
    _cancelButton.addActionListener(this);
    _cancelButton.setEnabled(false);
    buttonPanel.add(_cancelButton, gbc);

    pgbc.gridy++;
    _panel.add(buttonPanel, pgbc);

    pgbc.gridy++;
    pgbc.weighty = 2;
    pgbc.weightx = 2;
    pgbc.fill = GridBagConstraints.BOTH;
    _panel.add(new JLabel(""), pgbc);
    //_panel.add(new DebugLabel("Z",Color.BLUE), pgbc); 

    _dirty = false;
    updateButtons();
  }

  /**
   * Will load the current values for the preferences into the fields and return
   * the panel containing them to the caller.
   * @return panel
   */
  public JPanel getPanel() {
    reloadPrefs(null);
    return _panel;
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event instanceof SettingChangedEvent) {
      String setting = ((SettingChangedEvent) event).getName();
      reloadPrefs(setting);
    }
  }

  /**
   * Reset the value of the relevant field for a specified setting name from the
   * new current setting value. Pass null to reload all values.
   * @param setting
   */
  protected void reloadPrefs(String setting) {
    if(setting == null || ISettings.NUMBER_OF_BATTLES.equals(setting)) {
      int numberOfBattles = (int) _settingsService.getLong(ISettings.NUMBER_OF_BATTLES);
      _numberOfBattlesField.setText(numberOfBattles, 0);
    }

    if(setting == null || ISettings.SHOW_RAW_FP.equals(setting)) {
      _showRawFpField.setSelected(_settingsService.isFlagSet(ISettings.SHOW_RAW_FP));
    }

    if(setting == null || ISettings.F_CLASS_WORLD.equals(setting)) {
      long value = _settingsService.getLong(ISettings.F_CLASS_WORLD);
      _fClassWorldField.setText(value, 0);
    }

    if(setting == null || ISettings.E_CLASS_WORLD.equals(setting)) {
      long value = _settingsService.getLong(ISettings.E_CLASS_WORLD);
      _eClassWorldField.setText(value, 0);
    }

    if(setting == null || ISettings.D_CLASS_WORLD.equals(setting)) {
      long value = _settingsService.getLong(ISettings.D_CLASS_WORLD);
      _dClassWorldField.setText(value, 0);
    }

    if(setting == null || ISettings.C_CLASS_WORLD.equals(setting)) {
      long value = _settingsService.getLong(ISettings.C_CLASS_WORLD);
      _cClassWorldField.setText(value, 0);
    }

    if(setting == null || ISettings.B_CLASS_WORLD.equals(setting)) {
      long value = _settingsService.getLong(ISettings.B_CLASS_WORLD);
      _bClassWorldField.setText(value, 0);
    }

    if(setting == null || ISettings.A_CLASS_WORLD.equals(setting)) {
      long value = _settingsService.getLong(ISettings.A_CLASS_WORLD);
      _aClassWorldField.setText(value, 0);
      _sClassWorldLabel.setText("" + value);
    }

    if(setting == null || ISettings.DEFCON_5.equals(setting)) {
      long value = _settingsService.getLong(ISettings.DEFCON_5);
      _defcon5Field.setText(value, 0);
    }

    if(setting == null || ISettings.DEFCON_4.equals(setting)) {
      long value = _settingsService.getLong(ISettings.DEFCON_4);
      _defcon4Field.setText(value, 0);
    }

    if(setting == null || ISettings.DEFCON_3.equals(setting)) {
      long value = _settingsService.getLong(ISettings.DEFCON_3);
      _defcon3Field.setText(value, 0);
    }

    if(setting == null || ISettings.DEFCON_2.equals(setting)) {
      long value = _settingsService.getLong(ISettings.DEFCON_2);
      _defcon2Field.setText(value, 0);
      _defcon1Label.setText("" + value);
    }

    if(setting == null || ISettings.DEFAULT_SCANNER.equals(setting)) {
      long value = _settingsService.getLong(ISettings.DEFAULT_SCANNER);
      _defaultScanner.setText(value, 0);
    }

    if(setting == null) {
      _dirty = false;
      updateButtons();
    }

  }

  protected void savePrefs() {
    if(!isDataValid()) {
      throw new IllegalStateException("Form contains invalid values");
    }
    saveIfChanged(ISettings.NUMBER_OF_BATTLES, Convert.toInt(_numberOfBattlesField.getText()));
    saveIfChanged(ISettings.SHOW_RAW_FP, _showRawFpField.isSelected());

    saveIfChanged(ISettings.F_CLASS_WORLD, Convert.toInt(_fClassWorldField.getText()));
    saveIfChanged(ISettings.E_CLASS_WORLD, Convert.toInt(_eClassWorldField.getText()));
    saveIfChanged(ISettings.D_CLASS_WORLD, Convert.toInt(_dClassWorldField.getText()));
    saveIfChanged(ISettings.C_CLASS_WORLD, Convert.toInt(_cClassWorldField.getText()));
    saveIfChanged(ISettings.B_CLASS_WORLD, Convert.toInt(_bClassWorldField.getText()));
    saveIfChanged(ISettings.A_CLASS_WORLD, Convert.toInt(_aClassWorldField.getText()));

    saveIfChanged(ISettings.DEFCON_5, Convert.toInt(_defcon5Field.getText()));
    saveIfChanged(ISettings.DEFCON_4, Convert.toInt(_defcon4Field.getText()));
    saveIfChanged(ISettings.DEFCON_3, Convert.toInt(_defcon3Field.getText()));
    saveIfChanged(ISettings.DEFCON_2, Convert.toInt(_defcon2Field.getText()));

    saveIfChanged(ISettings.DEFAULT_SCANNER, Convert.toInt(_defaultScanner.getText()));
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    valueChanged(e.getDocument());
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    valueChanged(e.getDocument());
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    valueChanged(e.getSource());
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    valueChanged(e.getDocument());
  }

  public void valueChanged(Object field) {
    _log.debug("valueChanged");
    _dirty = true;
    updateButtons();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(e.getSource() instanceof JCheckBox) {
      valueChanged(e.getSource());
    } else if(e.getSource() == _saveButton) {
      savePrefs();
      _dirty = false;
      updateButtons();
    } else if(e.getSource() == _cancelButton) {
      reloadPrefs(null);
    }
  }

  protected boolean isDataValid() {
    for(IValidatingField field : _fields) {
      if(!field.isFieldValid()) {
        return false;
      }
    }
    return true;
  }

  protected void updateButtons() {
    _saveButton.setEnabled(_dirty && isDataValid());
    _cancelButton.setEnabled(_dirty);
  }

  /**
   * Checks to see if value is different to what is recorded for the specified
   * LONG setting, and if it is, saves new value to the setting.
   * @param setting
   * @param value
   */
  protected void saveIfChanged(String setting, long value) {
    //TODO - consider making this the normal behaviour of the setting service itself
    Objects.requireNonNull(setting, "setting must not be null");
    long current = _settingsService.getLong(setting);
    if(current != value) {
      _settingsService.setLong(setting, value);
    }
  }

  protected void saveIfChanged(String setting, boolean value) {
    //TODO - consider making this the normal behaviour of the setting service itself
    Objects.requireNonNull(setting, "setting must not be null");
    boolean current = _settingsService.isFlagSet(setting);
    if(current != value) {
      _settingsService.setFlag(setting, value);
    }
  }

}
