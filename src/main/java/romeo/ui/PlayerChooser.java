package romeo.ui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;

/**
 * Reusable component to facilitate selecting multiple players from a list.
 */
public class PlayerChooser implements IServiceListener, ItemListener {
  public static final String ALL_PLAYERS = "<<All Players>>";

  protected CheckBoxPanel _cbp;
  protected JComponent _component;
  protected IPlayerService _playerService;
  protected ItemListener _itemListener;

  public PlayerChooser(IPlayerService playerService) {
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _playerService.addListener(this);
    _cbp = new CheckBoxPanel();
    _cbp.setItemListener(this);
    prepChoices();
    JScrollPane cbpScroll = new JScrollPane(_cbp);
    cbpScroll.setPreferredSize(new Dimension(320, 128));
    cbpScroll.getVerticalScrollBar().setUnitIncrement(16);
    _component = cbpScroll;
  }

  public void setAll(boolean selected) {
    _cbp.setAll(selected);
  }

  /**
   * Stops listening to the Player Service to facilitate garbage collection and
   * nulls some internal references. The PlayerChooser can no longer be used
   * after this is called.
   */
  public void close() {
    _playerService.removeListener(this);
    _cbp = null;
  }

  public void setItemListener(ItemListener listener) {
    _itemListener = listener;
  }

  public JComponent getComponent() {
    return _component;
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event.getSource() instanceof IPlayerService) {
      prepChoices();
    }
  }

  protected void prepChoices() {
    Set<String> selections = getSelectedPlayers();
    _cbp.clearOptions();
    List<IPlayer> allPlayers = _playerService.getPlayers();
    if(allPlayers.size() > 0) {
      boolean selected = selections.contains(ALL_PLAYERS);
      _cbp.addCheckBox(ALL_PLAYERS, selected);
    }

    for(IPlayer player : allPlayers) {
      boolean selected = selections.contains(player.getName());
      _cbp.addCheckBox(player.getName(), selected);
    }
    //todo - need to reselect ones that  were selected before if we are just updating
    //the list
  }

  public Set<String> getSelectedPlayers() {
    return _cbp.getCheckedLabels();
  }

  //  public void setSelectedPlayers(Set<String> players)
  //  {
  //    _cbp.setCheckedLabels(players);
  //  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if(e.getSource() instanceof JCheckBox) {
      JCheckBox checkbox = (JCheckBox) e.getSource();
      if(ALL_PLAYERS.equals(checkbox.getText())) { //If the all players option is selected or deselected, we want to change the
                                                     //selection of all the checkboxes (except NOBODY!) and then send only 1 event
                                                   //to our listeners
        boolean selected = (ItemEvent.SELECTED == e.getStateChange());
        boolean nobodyState = _cbp.isSelected(IPlayer.NOBODY);
        _cbp.setAll(selected);
        _cbp.setSelected(IPlayer.NOBODY, nobodyState);
        _itemListener.itemStateChanged(e);
      } else if(_itemListener != null) {
        _itemListener.itemStateChanged(e);
      }
    }

  }
}
