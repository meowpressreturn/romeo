package romeo.ui.actions;

import java.util.Objects;

import romeo.players.api.IPlayer;
import romeo.players.impl.PlayerImpl;
import romeo.players.ui.PlayerFormFactory;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;

/**
 * Action for opening a form to create a new player record in the navigator
 * panel
 */
public class NewPlayerAction extends AbstractNewRecordAction {
  
  private final PlayerFormFactory _playerFormFactory;
  
  public NewPlayerAction(NavigatorPanel navigatorPanel, PlayerFormFactory playerFormFactory) {
    super(navigatorPanel);
    
    _playerFormFactory = Objects.requireNonNull(playerFormFactory, "playerFormFactory may not be null");
    
    setDescription("Create a Player record");
    setName("New Player");
    setIcon("/images/playerNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return _playerFormFactory.newPlayerForm(newRecord(), true);
  }

  private IPlayer newRecord() {
    return new PlayerImpl();
  }
}
