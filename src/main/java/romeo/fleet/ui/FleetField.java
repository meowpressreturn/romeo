package romeo.fleet.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import romeo.fleet.model.FleetElement;
import romeo.fleet.model.SourceId;
import romeo.fleet.ui.FleetFieldModel.FleetModelElement;
import romeo.model.api.IServiceListener;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.ui.BeanTableModel;
import romeo.ui.NumericCellRenderer;
import romeo.ui.forms.FieldChangeListenerList;
import romeo.ui.forms.IFieldChangeListener;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;

/**
 * UI component to display and edit units belonging to one side in a battle.
 * (This can include units from multiple fleets owned by that player as
 * indicated by the 'source')
 */
public class FleetField extends JPanel implements IServiceListener {
  protected static final int PREF_WIDTH = 350;
  protected static final int PREF_HEIGHT = 160;
  protected static final ImageIcon FLEET_IMAGE = GuiUtils.getImageIcon("/images/unit.gif");
  protected static final ImageIcon PLUS_IMAGE = GuiUtils.getImageIcon("/images/plus.gif");
  protected static final ImageIcon MINUS_IMAGE = GuiUtils.getImageIcon("/images/minus.gif");
  protected static final ImageIcon REMOVE_IMAGE = GuiUtils.getImageIcon("/images/cyanCross.gif");
  protected static final ImageIcon NORMALISE_IMAGE = GuiUtils.getImageIcon("/images/wand.gif");
  protected static final String FP_TXT = "Firepower=";
  protected static final String RAW_FP_TEXT = "Raw FP=";
  protected static final String PD_TXT = "PD=";
  protected static final String CARRY_TXT = "CA=";

  protected static final Vector<String> DEF_SOURCE_OPTIONS = new Vector<String>();
  static {
    DEF_SOURCE_OPTIONS.add("B:");
    DEF_SOURCE_OPTIONS.add("1:");
    DEF_SOURCE_OPTIONS.add("2:");
    DEF_SOURCE_OPTIONS.add("3:");
    DEF_SOURCE_OPTIONS.add("4:");
    DEF_SOURCE_OPTIONS.add("5:");
    DEF_SOURCE_OPTIONS.add("6:");
    DEF_SOURCE_OPTIONS.add("7:");
    DEF_SOURCE_OPTIONS.add("8:");
    DEF_SOURCE_OPTIONS.add("9:");
  }
  protected static final Vector<String> ATK_SOURCE_OPTIONS = new Vector<String>();
  static {
    ATK_SOURCE_OPTIONS.add("0:");
    ATK_SOURCE_OPTIONS.add("1:");
    ATK_SOURCE_OPTIONS.add("2:");
    ATK_SOURCE_OPTIONS.add("3:");
    ATK_SOURCE_OPTIONS.add("4:");
    ATK_SOURCE_OPTIONS.add("5:");
    ATK_SOURCE_OPTIONS.add("6:");
    ATK_SOURCE_OPTIONS.add("7:");
    ATK_SOURCE_OPTIONS.add("8:");
    ATK_SOURCE_OPTIONS.add("9:");
  }

  ////////////////////////////////////////////////////////////////////////////

  protected JTextArea _fleetText;
  protected JTable _fleetTable;
  protected FleetFieldModel _model;
  protected ISettingsService _settingsService;
  protected IUnitService _unitService;
  protected IXFactorService _xFactorService;
  protected Color _normalColor;
  protected JButton _normaliseButton;
  protected JButton _plusButton;
  protected JButton _minusButton;
  protected JButton _removeButton;
  protected JComboBox<IUnit> _unitCombo;
  protected TitledBorder _border;
  protected JScrollPane _fleetTextScroll;
  protected JScrollPane _fleetTableScroll;
  protected FieldChangeListenerList _fieldChangeListeners = new FieldChangeListenerList();
  protected String _testText = "";
  protected JComboBox<String> _sourceCombo;
  protected String _name;
  protected int _oldTotalQty = 0;
  protected boolean _showRawFp;

  public FleetField(String name,
                    ISettingsService settingsService,
                    IUnitService unitService,
                    IXFactorService xFactorService,
                    IXFactorCompiler compiler,
                    boolean defender) {
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService must not be null");
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService must not be null");
    _name = name;
    _model = new FleetFieldModel(this, unitService, compiler);
    _model.setDefender(defender);
    _model.addFieldChangeListener(new IFieldChangeListener() {
      @Override
      public void valueChanged(Object object) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updateDisplay();
            notifyListeners();
          }
        });
      }
    });

    _showRawFp = _settingsService.isFlagSet(ISettings.SHOW_RAW_FP);

    //Create components
    JButton testButton = new JButton();
    testButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(_testText != null) {
          setText(_testText);
        }
      }
    });
    _fleetText = new JTextArea();
    _normalColor = _fleetText.getBackground();
    _fleetText.setDocument(_model.getDocument());
    _fleetText.setWrapStyleWord(true);
    _fleetText.setLineWrap(true);
    JScrollPane textScroll = new JScrollPane(_fleetText);
    _fleetTextScroll = textScroll;
    textScroll.getVerticalScrollBar().setUnitIncrement(16);
    textScroll.setPreferredSize(new Dimension(PREF_WIDTH, (PREF_HEIGHT / 5) * 2));
    textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    _fleetTable = new JTable(_model.getTableModel());
    _fleetTable.setDefaultRenderer(Double.class, new NumericCellRenderer(2));
    _fleetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    //Listener that will update the combo boxes etc above the table when the user selects a row in the table
    _fleetTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if(!lsm.isSelectionEmpty()) {
          BeanTableModel tableModel = (BeanTableModel)_model.getTableModel();
          FleetElement row = (FleetElement)tableModel.getRowBean(lsm.getMinSelectionIndex());
          _unitCombo.setSelectedItem( row.getUnit() );          
          int sourceId = row.getSource().asInteger(); //nb: FleetElements dont allow the ANY source so this isnt null
          Vector<String> options = _model.isDefender() ? DEF_SOURCE_OPTIONS : ATK_SOURCE_OPTIONS;
          if(sourceId < options.size()) {
            //The combo box supports only a limited number of source fleets, so if the source id is beyond that
            //we shall just ignore it for now. (TODO issue #152)
            _sourceCombo.setSelectedItem( options.get(sourceId) );
          }          
        }
      }
    });
    GuiUtils.setColumnWidths(_fleetTable, new int[] { 10, 90, 40, 20, 32, 32, 16, 20 });
    TableColumn xfActiveColumn = _fleetTable.getColumnModel().getColumn(0);

    xfActiveColumn.setCellRenderer(new XFactorColumnRenderer());
    xfActiveColumn.setMaxWidth(10);
    xfActiveColumn.setMinWidth(10);

    JScrollPane tableScroll = new JScrollPane(_fleetTable);
    tableScroll.getVerticalScrollBar().setUnitIncrement(16);
    _fleetTableScroll = tableScroll;
    tableScroll.setPreferredSize(new Dimension(PREF_WIDTH, (PREF_HEIGHT / 5) * 3));
    tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    tableScroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, testButton); //Test button
    _normaliseButton = new JButton(NORMALISE_IMAGE);
    _normaliseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String text = _model.makeNormalisedText();
        _fleetText.setText(""); //Hack to workaround stupid Swing issue
        _fleetText.setText(text);
      }
    });

    _sourceCombo = new JComboBox<String>(defender ? DEF_SOURCE_OPTIONS : ATK_SOURCE_OPTIONS);
    _sourceCombo.setMaximumRowCount(10);

    _unitCombo = new JComboBox<IUnit>(new Vector<IUnit>(_unitService.getUnits()));
    _unitCombo.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {
        IUnit unit = (IUnit) value;
        if(unit == null) {
          //Special case which occurs when there are no units 
          return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        } else {
          Component c = super.getListCellRendererComponent(list, unit.getName(), index, isSelected, cellHasFocus);
          if(c instanceof JComponent) {
            String text = "(" + unit.getAcronym() + ") " + unit.getName() + " " + unit.getAttacks() + " * "
                + unit.getOffense() + "/" + unit.getDefense() + " FP=" + Convert.toStr(unit.getFirepower(), 2);
            ((JComponent) c).setToolTipText(text);
          }
          return c;
        }
      }
    });
    _plusButton = new JButton(PLUS_IMAGE);
    _plusButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IUnit unit = (IUnit) _unitCombo.getSelectedItem();
        if(unit != null) {
          int addSource = getSelectedSource();
          FleetElement addOne = new FleetElement(unit, 1, SourceId.fromInt(addSource));
          _model.getFleetContents().addElement(addOne);

          String text = _model.makeNormalisedText();
          _fleetText.setText(text);
        }
      }
    });
    _minusButton = new JButton(MINUS_IMAGE);
    _minusButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IUnit unit = (IUnit) _unitCombo.getSelectedItem();
        if(unit != null) {
          int subtractSource = getSelectedSource();
          FleetElement subtractOne = new FleetElement(unit, -1, SourceId.fromInt(subtractSource));
          _model.getFleetContents().addElement(subtractOne);
          String text = _model.makeNormalisedText();
          _fleetText.setText(" "); //hacky workaround for Document issue
          _fleetText.setText(text);
        }
      }
    });
    _removeButton = new JButton(REMOVE_IMAGE);
    _removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IUnit unit = (IUnit) _unitCombo.getSelectedItem();
        if(unit != null) {
          int removeSource = getSelectedSource();
          FleetElement profile = new FleetElement(unit, 0, SourceId.fromInt(removeSource));
          _model.getFleetContents().removeElement(profile);
          String text = _model.makeNormalisedText();
          _fleetText.setText(" "); //hacky workaround for Document issue
          _fleetText.setText(text);
        }
      }
    });

    //Prepare the controls panel layout
    Dimension bSize = new Dimension(32, 24);
    JPanel controlsPanel = new JPanel();
    GridBagConstraints controlsPanelGBL = GuiUtils.prepGridBag(controlsPanel);
    controlsPanelGBL.insets = new Insets(0, 0, 0, 0);
    controlsPanelGBL.weightx = 0;
    controlsPanelGBL.fill = GridBagConstraints.BOTH;
    _normaliseButton.setPreferredSize(bSize);
    _normaliseButton.setMaximumSize(bSize);
    controlsPanel.add(_normaliseButton, controlsPanelGBL);
    controlsPanelGBL.gridx++;

    Dimension sourceSize = new Dimension(48, 24);
    _sourceCombo.setPreferredSize(sourceSize);
    _sourceCombo.setMinimumSize(sourceSize);
    controlsPanel.add(_sourceCombo, controlsPanelGBL);
    controlsPanelGBL.gridx++;

    controlsPanelGBL.weightx=1;
    //_unitCombo.setPreferredSize(new Dimension((PREF_WIDTH / 5) * 3, 24));
    controlsPanel.add(_unitCombo, controlsPanelGBL);
    controlsPanelGBL.weightx=0;
    controlsPanelGBL.gridx++;

    controlsPanel.add(_plusButton, controlsPanelGBL);
    controlsPanelGBL.gridx++;

    controlsPanel.add(_minusButton, controlsPanelGBL);
    controlsPanelGBL.gridx++;

    _removeButton.setPreferredSize(bSize);
    _removeButton.setMaximumSize(bSize);
    controlsPanel.add(_removeButton, controlsPanelGBL);
    controlsPanelGBL.gridx++;

    //Border outside = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    Border outside = BorderFactory.createEmptyBorder();
    _border = BorderFactory.createTitledBorder("Test");
    setBorder(BorderFactory.createCompoundBorder(outside, _border));

    //Now add all to layout
    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.weightx=1; //expand horizontally to fill free space in this panel

    gbc.gridy++;
    add(textScroll, gbc);

    gbc.gridy++;
    add(controlsPanel, gbc);

    gbc.gridy++;
    add(tableScroll, gbc);

    updateDisplay();

    _settingsService.addListener(this);
    _unitService.addListener(this);
    _xFactorService.addListener(this);
  }
  
  @Override
  public String toString() {
    return "FleetField[" + _name + "]";
  }

  /**
   * Implemented to satisfy the {@link IServiceListener} interface, this will
   * update the field when it is notified of changes by a service.
   * @param service
   */
  @Override
  public void dataChanged(EventObject event) {
    if(event instanceof SettingChangedEvent) {
      SettingChangedEvent sce = (SettingChangedEvent) event;
      String setting = sce.getName();
      if(ISettings.SHOW_RAW_FP.equals(setting)) {
        _showRawFp = (Boolean) sce.getValue();
      }
    } else {
      IUnit oldSelection = (IUnit) _unitCombo.getSelectedItem();
      Vector<IUnit> units = new Vector<IUnit>(_unitService.getUnits());
      ComboBoxModel<IUnit> newModel = new DefaultComboBoxModel<IUnit>(units);
      _unitCombo.setModel(newModel);
      if(oldSelection != null) {
        int i = units.indexOf(oldSelection);
        _unitCombo.setSelectedIndex(i);
      }
    }
    _model.update(false);
  }

  /**
   * Will cause the field to remove itself from the listener list of the
   * {@link IUnitService} and the {@link IXFactorService}. This should be called
   * before discarding a FleetField to allow it to be garbage collected.
   */
  public void stopListening() {
    _unitService.removeListener(this);
    _xFactorService.removeListener(this);
    _settingsService.removeListener(this);
  }

  /**
   * Update the fleet field title label with the specified fp and pd
   * @param fp
   *          firepower
   * @param pd
   *          population damage metric
   * @param ca
   *          carry (may be negative)
   */
  protected void setFleetTitleStats(double fp, double rawFp, double pd, double ca) {
    String name = (_name == null) ? "" : _name;
    String text = FP_TXT + Convert.toStr(fp, 2) + (_showRawFp ? (", " + RAW_FP_TEXT + Convert.toStr(rawFp, 2)) : "")
        + ", " + PD_TXT + Convert.toStr(pd, 0) + ", " + CARRY_TXT + Convert.toStr(ca, 0);
    _border.setTitle(name + " (" + text + ")");
    this.repaint();
  }

  @Override
  public String getName() {
    return _name;
  }

  protected int getSelectedSource() {
    String srcText = (String) _sourceCombo.getSelectedItem();
    if("B:".equals(srcText))
      return 0;
    int indexOfColon = srcText.indexOf(':');
    if(indexOfColon == -1)
      indexOfColon = srcText.length();
    srcText = srcText.substring(0, indexOfColon);
    int result = Convert.toInt(srcText);
    return result;
  }

  public void addFieldChangeListener(IFieldChangeListener l) {
    _fieldChangeListeners.addFieldChangeListener(l);
  }

  public void removeFieldChangeListener(IFieldChangeListener l) {
    _fieldChangeListeners.removeFieldChangeListener(l);
  }

  protected void updateDisplay() {
    Highlighter h = _fleetText.getHighlighter();
    Highlighter.HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);//DefaultHighlighter.DefaultPainter;
    h.removeAllHighlights();
    //System.out.println("FleetField.updateDisplay() called at " + System.currentTimeMillis());
    List<FleetFieldModel.FleetModelElement> elements = _model.getElements();
    for(int i = 0; i < elements.size(); i++) {
      FleetFieldModel.FleetModelElement element = (FleetFieldModel.FleetModelElement) elements.get(i);
      if(element.isValid() == false) {
        try {
          h.addHighlight(element.getStart(), element.getStart() + element.getLength(), p);
        } catch(BadLocationException ble) {
          throw new RuntimeException("Failed to highlight", ble);
        }
      }
    }
    double totalFp = _model.getTotalFirepower(false); //total fp with xFactors
    double rawFp = _model.getTotalFirepower(true); //total fp without xFactors applied
    double totalPd = _model.getTotalPd();
    double totalCa = _model.getTotalCarry();
    setFleetTitleStats(totalFp, rawFp, totalPd, totalCa);
  }

  public String getText() {
    return _fleetText.getText();
  }

  public void setText(String text) {
    _oldTotalQty = _oldTotalQty == 0 ? Integer.MAX_VALUE : 0; //Ensure that our listeners get to know
    _fleetText.setText(text);
  }

  public FleetFieldModel getModel() {
    return _model;
  }

  protected void notifyListeners() {
    //Count number of units (including noncoms) now in fleet. If this is different
    //from what it was before then we notify the listeners and update
    //out record of the total. We assume that it is not possible to substitute
    //values such that totalQty is unchanged but fleet contents are different
    //in an atomic way that avoids going through this method
    int newTotalQty = 0;
    newTotalQty = _model.getFleetContents().getSize(false);
    if(newTotalQty != _oldTotalQty) {
      _fieldChangeListeners.notifyFieldChangeListeners(this);
    }
  }

  /**
   * Returns a list of the types of units that are in the field. Types listed
   * multiple times in the text field are only reported once in this list. Other
   * than that, the units in the list appear in the order they appear in the
   * elements list. This may be different from the order they appear in the
   * table and fleetMap.
   * @return types A List of Unit instances
   */
  public List<IUnit> getSelectedTypes() {
    List<FleetModelElement> elements = _model.getElements();
    ArrayList<IUnit> types = new ArrayList<IUnit>(elements.size());
    for(FleetModelElement element : elements) {
      IUnit unit = element.getUnit();
      if(unit != null && types.contains(unit) == false) {
        types.add(unit);
      }
    }
    return types;
  }

  public String getTestText() {
    return _testText;
  }

  public void setTestText(String string) {
    _testText = string;
  }

}
