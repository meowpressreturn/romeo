package romeo.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import romeo.utils.GuiUtils;

/**
 * Creates and maintains a panel with radio buttons for selecting a history
 * statistic (eg: worlds, firepower etc). Implements ItemSelectable, but will
 * only inform listeners of stat selection and not deselection.
 */
public class StatSelector implements ItemSelectable {
  public static final String LABEL_WORLDS = "Worlds";
  public static final String LABEL_LABOUR = "Labour";
  public static final String LABEL_CAPITAL = "Capital";
  public static final String LABEL_FIREPOWER = "Firepower";

  private JComponent _component;
  private JRadioButton _selectedHistory;
  private List<ItemListener> _listenerList = new LinkedList<>();

  /**
   * Constructor. Will create the radio buttons and add them to the panel. The
   * "worlds" stat will be selected by default.
   */
  public StatSelector() {
    JRadioButton worldsRadio = new JRadioButton(LABEL_WORLDS, true);
    JRadioButton labourRadio = new JRadioButton(LABEL_LABOUR, false);
    JRadioButton capitalRadio = new JRadioButton(LABEL_CAPITAL, false);
    JRadioButton firepowerRadio = new JRadioButton(LABEL_FIREPOWER, false);
    _selectedHistory = worldsRadio; //nb: to set you also need to change which of above buttons has 'true' for selected flag
    ButtonGroup statGroup = new ButtonGroup();
    statGroup.add(worldsRadio);
    statGroup.add(labourRadio);
    statGroup.add(capitalRadio);
    statGroup.add(firepowerRadio);
    ItemListener listener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if(ItemEvent.SELECTED == e.getStateChange()) { //Only update for the radio that was selected (not for the one deselected)
          _selectedHistory = (JRadioButton) e.getItem();
          fireEvent();
        }
      }
    };
    worldsRadio.addItemListener(listener);
    labourRadio.addItemListener(listener);
    capitalRadio.addItemListener(listener);
    firepowerRadio.addItemListener(listener);
    JPanel statPanel = new JPanel();
    GridBagConstraints gbc = GuiUtils.prepGridBag(statPanel);
    gbc.insets = new Insets(4, 8, 0, 4);
    statPanel.add(worldsRadio, gbc);
    gbc.gridy++;
    statPanel.add(labourRadio, gbc);
    gbc.gridy++;
    statPanel.add(capitalRadio, gbc);
    gbc.gridy++;
    statPanel.add(firepowerRadio, gbc);
    gbc.gridy++;
    //gbc.gridx++;
    //gbc.weightx=1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    JLabel filler = new JLabel();
    statPanel.add(filler, gbc);
    //filler.setText("filler");
    //filler.setBackground(Color.RED);
    //statPanel.setBackground(Color.YELLOW);

    //This doesnt work for some reason...
    //If as needed, it never shows when squeezed, if always, its unsqueezable!
    JScrollPane scroll = new JScrollPane(statPanel);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    GuiUtils.initScrollIncrement(scroll);

    _component = scroll;
  }

  /**
   * Return a component that can be added to the ui
   * @return component
   */
  public JComponent getComponent() {
    return _component;
  }

  /**
   * Return the stat that is currently selected
   * @return stat
   */
  public String getSelectedStatistic() {
    return _selectedHistory.getText();
  }

  //TODO - write a setter when we need one (which we dont yet)

  @Override
  public void addItemListener(ItemListener l) {
    if(!_listenerList.contains(l)) {
      _listenerList.add(l);
    }
  }

  @Override
  public void removeItemListener(ItemListener l) {
    _listenerList.remove(l);
  }

  @Override
  public Object[] getSelectedObjects() {
    return new Object[] { _selectedHistory };
  }

  /**
   * Create a SELECTED event and fire to all the listeners.
   */
  protected void fireEvent() {
    ItemEvent e = null;
    for(ItemListener listener : _listenerList) {
      if(e == null) {
        e = new ItemEvent(this, 0, _selectedHistory, ItemEvent.SELECTED);
      }
      listener.itemStateChanged(e);
    }
  }
}
