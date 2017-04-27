package romeo.ui.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import romeo.Romeo;
import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;

/**
 * Field that allows entry of a player name, either via free entry or via a
 * combo dropdown. The values is set and get as a String.
 */
public class PlayerCombo extends JComboBox<Object>
    implements ListCellRenderer<Object>, IValidatingField, IServiceListener {
  private DefaultListCellRenderer _renderer = new DefaultListCellRenderer();
  private IPlayerService _service;
  private boolean _fieldValid = true;
  private Color _normalBg;
  private boolean _mandatory = false;
  private String _initYet = "Yes";

  public PlayerCombo() {
    init(Romeo.CONTEXT.getPlayerService());
    prepareOptions();
    setPlayerName("");
  }

  private void init(IPlayerService service) {
    super.setEditable(true);
    setRenderer(this);
    service.addListener(this);
    _service = service;
    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        updateValidity();
      }
    });
  }

  @Override
  public void dataChanged(EventObject event) {
    prepareOptions();
  }

  protected void prepareOptions() {
    String playerName = getPlayerName();

    ActionListener[] a = getActionListeners();
    //Remove all ActionListeners to avoid unwanted notifications as we change options
    for(int i = 0; i < a.length; i++)
      removeActionListener(a[i]);

    List<IPlayer> players = loadRecords();
    if(players == null)
      players = Collections.emptyList();
    removeAllItems();
    for(IPlayer player : players) {
      addItem(player.getName());
    }

    setPlayerName(playerName);

    //Replace all ActionListeners
    for(int i = 0; i < a.length; i++)
      addActionListener(a[i]);
  }

  protected List<IPlayer> loadRecords() {
    return _service.getPlayers();
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    String display = null;
    if(value instanceof IPlayer) {
      display = ((IPlayer) value).getName();
    } else {
      display = value == null ? "" : value.toString();
    }
    return _renderer.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
  }

  protected void updateValidity() {
    setFieldValid(true); //Currently no check on player validity as its free entry
    setBackground(isFieldValid() ? _normalBg : Color.RED);
  }

  public IPlayerService getService() {
    return _service;
  }

  @Override
  public void setBackground(Color bg) {
    if(isFieldValid() && _initYet != null) {
      _normalBg = bg;
      super.setBackground(bg);
    } else {
      super.setBackground(bg);
    }
  }

  @Override
  public boolean isFieldValid() {
    return _fieldValid;
  }

  public void setFieldValid(boolean b) {
    _fieldValid = b;
  }

  public boolean isMandatory() {
    return _mandatory;
  }

  public void setMandatory(boolean mandatory) {
    _mandatory = mandatory;
  }

  public String getPlayerName() {
    return (String) getModel().getSelectedItem();
  }

  public void setPlayerName(String player) {
    getModel().setSelectedItem(player);
  }

}
