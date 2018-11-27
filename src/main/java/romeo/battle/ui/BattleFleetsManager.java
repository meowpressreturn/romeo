package romeo.battle.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import romeo.battle.IBattleCalculator;
import romeo.fleet.ui.FleetField;
import romeo.settings.api.ISettingsService;
import romeo.units.api.IUnitService;
import romeo.utils.GuiUtils;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;

/**
 * Manages the {@link FleetField} instances used to edit the fleets in the
 * battle simulator. FleetFields are always provided for the attacker and
 * defender, and the user can add extra fields to enter details of additional
 * parties to multiway battles.
 */
public class BattleFleetsManager implements ActionListener {
  protected static final ImageIcon ADD_IMAGE = GuiUtils.getImageIcon("/images/plus.gif");
  protected static final ImageIcon REMOVE_IMAGE = GuiUtils.getImageIcon("/images/minus.gif");
  protected static final ImageIcon RESET_IMAGE = GuiUtils.getImageIcon("/images/cyanCross.gif");

  /**
   * Set of preset names used to identify fleets. These are used as keys so must
   * be unique. In practice I doubt anyone simulates more than about 3 or 4
   * players in a battle.
   */
  private static final String[] FLEET_NAMES = new String[] { "Defender", "Attacker", "Third Party", "Company", "Kilroy",
      "Erowhon", "Marian", "Wilhelmina", "Shizuka", "Blade", "Maria", "Gordon", "Manuel", "Kaspar", "Kitty", "Red",
      "Abdul", "Danforth", "Claw", "Bandersnatch", "Scrooge", "Apprentice", "Doctor", "Houston", "Apollo", "Sudohbucks",
      "Genki", "Saga", "Lotus", "Larry", "Godwin", "Dodd", "Pacific", "Winston", "Lowry",
      "Sir Not-Appearing-In-This-Program", }; //remember to keep kaspar as 14th (index 13) :-)

  /**
   * To get the preset fleet name for a given fleet index you must use this
   * function. This is because the list of names is finite. This function will
   * generate a name for those beyond the list. (eg "Player42")
   * @param index
   * @return
   */
  protected static String presetFleetName(int index) {
    return (index >= FLEET_NAMES.length) ? "Player" + index : FLEET_NAMES[index];
  }

  /////////////////////////////////////////////////////////////////////////////

  protected ISettingsService _settingsService;
  protected IUnitService _unitService;
  protected IXFactorService _xfactorService;
  protected IXFactorCompiler _compiler;
  protected JPanel _mainPanel;
  protected JPanel _fleetPanel;
  protected JButton _addButton;
  protected JButton _removeButton;
  protected JButton _resetButton;

  private Map<String, FleetField> _fleets = new LinkedHashMap<String, FleetField>(); //FleetField by name

  /**
   * Constructor.
   * @param unitService
   * @param unitService
   * @param xFactorService
   * @param compiler
   */
  public BattleFleetsManager(ISettingsService settingsService,
                             IUnitService unitService,
                             IXFactorService xFactorService,
                             IXFactorCompiler compiler) {
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService must not be null");
    _xfactorService = Objects.requireNonNull(xFactorService, "xFactorService must not be null");

    _compiler = compiler;

    _fleetPanel = new JPanel();
    _fleetPanel.setLayout(new BoxLayout(_fleetPanel, BoxLayout.Y_AXIS));

    _addButton = new JButton("Add Player", ADD_IMAGE);
    _addButton.addActionListener(this);
    _removeButton = new JButton("Remove Player", REMOVE_IMAGE);
    _removeButton.addActionListener(this);
    _resetButton = new JButton("Clear", RESET_IMAGE);
    _resetButton.addActionListener(this);

    JPanel controlPanel = new JPanel();
    GridBagConstraints controlGbc = GuiUtils.prepGridBag(controlPanel);
    controlGbc.insets = new Insets(1, 1, 0, 0);
    controlGbc.weightx = 1;
    controlPanel.add(_addButton, controlGbc);
    controlGbc.gridx++;
    controlPanel.add(_removeButton, controlGbc);
    controlGbc.gridx++;
    controlPanel.add(_resetButton, controlGbc);

    _mainPanel = new JPanel();
    _mainPanel.setLayout(new BoxLayout(_mainPanel, BoxLayout.Y_AXIS));
    _mainPanel.add(controlPanel);
    _mainPanel.add(Box.createVerticalStrut(3));
    _mainPanel.add(_fleetPanel);

    resetFleets();
  }

  /**
   * Returns the name of the defending fleet
   * @return defenderName
   */
  public String getDefenderName() {
    return presetFleetName(0);
  }

  /**
   * Returns the names of the fleets
   * @return names
   */
  public String[] getNames() {
    String[] names = new String[_fleets.size()];
    int i = 0;
    for(FleetField field : _fleets.values()) {
      names[i++] = field.getName();
    }
    return names;
  }

  /**
   * Resets the fleets to the default attacker and defender fleets with no
   * contents added. Sets the testText values. The first fleet will be set as
   * defender (in addFleetField).
   */
  public void resetFleets() {
    while(getSize() > 0) { //Remove all the fleet fields (including unlinking them as service listeners)
      removeFleetField();
    }

    FleetField fleet1 = addFleetField(); //Defender
    FleetField fleet2 = addFleetField(); //Attacker        

    fleet2.setTestText("70 * X5, 5 * Org,10*S200,20*CB");
    fleet1.setTestText("35 * ATR, 2 * XMC, 2 * Hauler,10*S80");
  }

  /**
   * Exposes the reference to the component containing the fleetfields.
   * @return component
   */
  public JComponent getComponent() {
    return _mainPanel;
  }

  /**
   * Reads the field values into the battle calculator
   * @param bc
   */
  public void read(IBattleCalculator bc) {
    for(FleetField field : _fleets.values()) {
      stupidHackySwingFieldUpdateFix(field);

      String player = field.getName();
      bc.setFleet(player, field.getModel().getFleetContents());
    }
    bc.setDefender(getDefenderName());
  }

  /**
   * To be called before attempting to get the fleetContents from the field's
   * model as for some reason the Document isn't up to date with what's
   * currently in the field's text
   * @param field
   */
  private void stupidHackySwingFieldUpdateFix(FleetField field) {
    //WORKAROUND for the nonupdated deletion from field issue in swing (Dont remove)
    field.setText(field.getText());
    //id sure love to know how the field knows what the correct text is when its own document doesnt....
    ////////////
  }

  /**
   * Returns the contents of all the fleet fields as text
   * @return
   */
  public List<String> getFleetValues() {
    int l = _fleets.size();
    List<String> values = new ArrayList<>(l);
    //We need to retrieve them in order, so must use the preset player name keys
    for(int i = 0; i < l; i++) {
      String player = presetFleetName(i);
      FleetField field = _fleets.get(player);
      //stupidHackySwingFieldUpdateFix(field);
      String text = field.getText();
      values.add(text);
    }
    return values;
  }

  /**
   * Reset and initialise the contents of the fleet fields using the supplied
   * text values
   * @param values
   */
  public void setFleetsFromValues(List<String> values) {
    Objects.requireNonNull(values);
    resetFleets();
    int l = values.size();
    for(int i = 0; i < l; i++) {
      String player = presetFleetName(i);
      if(i >= _fleets.size()) {
        addFleetField();
      }
      FleetField field = _fleets.get(player);
      String value = values.get(i);
      field.setText(value);
    }
  }

  /**
   * Updates the enablement of the remove button and calls revalidate and
   * repaint on the mainPanel
   */
  protected void refreshView() {
    _removeButton.setEnabled(_fleets.size() > 2);
    _mainPanel.revalidate();
    _mainPanel.repaint();
  }

  /**
   * Gets the named fleet field. If a name is not supplied will return the
   * newest field
   * @param name
   * @return field
   */
  public FleetField getFleetField(String name) {
    if(name == null) {
      //TODO - use the preset list to get it directly please
      Iterator<FleetField> i = _fleets.values().iterator();
      FleetField field = null;
      while(i.hasNext())
        field = (FleetField) i.next(); //Move to last one
      return field;
    } else {
      FleetField field = (FleetField) _fleets.get(name);
      return field;
    }
  }

  /**
   * Create a new fleetfield in the fleets panel. Name will be assigned based on
   * position. A reference to the newly created field is returned.
   * @return field
   */
  public FleetField addFleetField() {
    int size = getSize();
    String name = presetFleetName(size);
    //Create the field. If its the first, mark it as the defender
    boolean isDefender = (size == 0);
    FleetField field = new FleetField(name, _settingsService, _unitService, _xfactorService, _compiler, isDefender);
    field.getModel().setBattleFleetsManager(this);
    field.setTestText("50 * Mek, 2 * RCW, 10*CB, 100 * SOG");
    _fleetPanel.add(field, 0);
    _fleets.put(name, field);
    refreshView();
    return field;
  }

  /**
   * Removes the last fleet field. This method can remove any fleet field.
   * Unlike the button listener code it does not check that there are at least
   * 2.
   * @param name
   */
  public void removeFleetField() {
    if(_fleets.size() == 0)
      return;
    FleetField field = getFleetField(null);
    field.stopListening();
    _fleetPanel.remove(field);
    _fleets.remove(field.getName());
    refreshView();
  }

  /**
   * Returns the number of fleet fields
   * @return size
   */
  public int getSize() {
    return _fleets.size();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();

    if(_addButton.equals(source)) {
      addFleetField();
    }

    if(_removeButton.equals(source) && (getSize() > 2)) { //nb we only allow fleets beyond the first two to be removed here
      removeFleetField();
    }

    if(_resetButton.equals(source)) { //Clear the fleets & reset to 2 empty fields
      resetFleets();
    }
  }
}
