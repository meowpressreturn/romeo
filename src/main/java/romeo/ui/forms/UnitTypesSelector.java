package romeo.ui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.List;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import romeo.model.api.IServiceListener;
import romeo.ui.BeanTableModel;
import romeo.ui.NumericCellRenderer;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

public class UnitTypesSelector extends JPanel {

  //this whole subclass is starting to stink. It really needs its own
  //impl altogether

  protected static final ImageIcon TICK_ICON = GuiUtils.getImageIcon("/images/tick.gif");
  protected static final ImageIcon CROSS_ICON = GuiUtils.getImageIcon("/images/cross.gif");

  protected JTextArea _unitsText;
  protected JTable _unitsTable;
  protected UnitTypesSelectorModel _model;
  protected IUnitService _unitService;
  protected Color _normalColor;
  protected JButton _tickButton;
  protected JButton _crossButton;
  protected JComboBox<IUnit> _unitTypeCombo;
  protected JScrollPane _unitsTextScroll;
  protected JScrollPane _unitsTableScroll;
  protected FieldChangeListenerList _fieldChangeListeners = new FieldChangeListenerList();

  protected static final int PREF_WIDTH = 360; //not quite working properly
  protected static final int PREF_HEIGHT = 128;

  public UnitTypesSelector(IUnitService unitService) {
    _unitService = unitService;
    _model = new UnitTypesSelectorModel(unitService);
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

    //Create components
    JButton testButton = new JButton();
    _unitsText = new JTextArea();
    _normalColor = _unitsText.getBackground();
    _unitsText.setDocument(_model.getDocument());
    _unitsText.setWrapStyleWord(true);
    _unitsText.setLineWrap(true);
    _unitsTextScroll = new JScrollPane(_unitsText);
    _unitsTextScroll.setPreferredSize(new Dimension(PREF_WIDTH, 24));
    _unitsTextScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    _unitsTable = new JTable(_model.getTableModel());
    _unitsTable.setDefaultRenderer(Double.class, new NumericCellRenderer(2));
    _unitsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _unitsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if(!lsm.isSelectionEmpty()) {
          TableModel tableModel = _model.getTableModel();
          if(tableModel instanceof BeanTableModel) {
            Object row = ((BeanTableModel) tableModel).getRowBean(lsm.getMinSelectionIndex());
            IUnit unit = (IUnit) row;
            _unitTypeCombo.setSelectedItem(unit);
          }
        }
      }
    });
    GuiUtils.setColumnWidths(_unitsTable, new int[] { 90, 24, 24, 24, 24 });
    _unitsTableScroll = new JScrollPane(_unitsTable);
    _unitsTableScroll.setPreferredSize(new Dimension(PREF_WIDTH, (PREF_HEIGHT - 24 - 32)));
    _unitsTableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    _unitsTableScroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, testButton); //Test button

    _unitTypeCombo = new JComboBox<IUnit>(new Vector<IUnit>(_unitService.getUnits()));
    _unitTypeCombo.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
          boolean cellHasFocus) {
        IUnit unit = (IUnit) value;
        if(unit == null) {
          //special case that occurs when there are no units
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
    _tickButton = new JButton(TICK_ICON);
    _tickButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IUnit unit = (IUnit) _unitTypeCombo.getSelectedItem();
        if(unit != null) {
          _model.addUnit(unit);
          String text = _model.makeNormalisedText();
          _unitsText.setText(text);
        }
      }
    });
    _crossButton = new JButton(CROSS_ICON);
    _crossButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        IUnit unit = (IUnit) _unitTypeCombo.getSelectedItem();
        if(unit != null) {
          _model.removeUnit(unit);
          String text = _model.makeNormalisedText();
          _unitsText.setText(" "); //hacky workaround for Document issue
          _unitsText.setText(text);
        }
      }
    });

    JPanel controlsPanel = new JPanel();
    GridBagConstraints cgbc = GuiUtils.prepGridBag(controlsPanel);
    cgbc.insets = new Insets(0, 0, 0, 0);
    cgbc.weightx = 1;
    cgbc.fill = GridBagConstraints.BOTH;

    _unitTypeCombo.setPreferredSize(new Dimension((PREF_WIDTH / 5) * 3, 24));
    controlsPanel.add(_unitTypeCombo, cgbc);
    cgbc.gridx++;

    controlsPanel.add(_tickButton, cgbc);
    cgbc.gridx++;

    controlsPanel.add(_crossButton, cgbc);
    cgbc.gridx++;

    //Now add all to layout
    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = new Insets(0, 0, 0, 0);
    add(_unitsTextScroll, gbc);

    gbc.gridy++;
    add(controlsPanel, gbc);
    gbc.gridy++;
    add(_unitsTableScroll, gbc);

    updateDisplay();

    setBorder(BorderFactory.createEtchedBorder());

    revalidate(); //surely i dont need this in a constructor?

    _unitService.addListener(new IServiceListener() {
      @Override
      public void dataChanged(EventObject event) {
        IUnit oldSelection = (IUnit) _unitTypeCombo.getSelectedItem();
        Vector<IUnit> units = new Vector<IUnit>(_unitService.getUnits());
        ComboBoxModel<IUnit> newModel = new DefaultComboBoxModel<IUnit>(units);
        _unitTypeCombo.setModel(newModel);
        if(oldSelection != null) {
          int i = units.indexOf(oldSelection);
          _unitTypeCombo.setSelectedIndex(i);
        }
        _model.update();
      }
    });
  }

  public void addFieldChangeListener(IFieldChangeListener l) {
    _fieldChangeListeners.addFieldChangeListener(l);
  }

  public void removeFieldChangeListener(IFieldChangeListener l) {
    _fieldChangeListeners.removeFieldChangeListener(l);
  }

  protected void updateDisplay() {
    Highlighter h = _unitsText.getHighlighter();
    Highlighter.HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);//DefaultHighlighter.DefaultPainter;
    h.removeAllHighlights();

    List<UnitTypesSelectorModel.Element> elements = _model.getElements();
    for(int i = 0; i < elements.size(); i++) {
      UnitTypesSelectorModel.Element element = (UnitTypesSelectorModel.Element) elements.get(i);
      if(element._unit == null) {
        try {
          h.addHighlight(element._start, element._start + element._length, p);
        } catch(Exception whyIsThisNotARuntime) {
          throw new RuntimeException("Failed to highlight", whyIsThisNotARuntime);
        }
      }
    }
  }

  public String getText() {
    return this._unitsText.getText();
  }

  public void setText(String text) {
    //_oldTotalQty = _oldTotalQty == 0 ? Integer.MAX_VALUE : 0; //Ensure that our listeners get to know
    _unitsText.setText(text);
  }

  public UnitTypesSelectorModel getModel() {
    return _model;
  }

  protected void notifyListeners() {
    //newTotalQty = _model.getFleetContents().getSize();
    //if(newTotalQty != _oldTotalQty)
    _fieldChangeListeners.notifyFieldChangeListeners(this);
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
    return _model.getSelectedTypes();
  }
}
