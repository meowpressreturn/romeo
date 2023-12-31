package romeo.players.ui;

import java.util.Map;
import java.util.Objects;

import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerId;
import romeo.players.impl.PlayerImpl;
import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;

/**
 * Opens a Player Form in the {@link NavigatorPanel}
 */
public class PlayerNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {
  
  private IPlayerService _playerService;
  private PlayerFormFactory _playerFormFactory;

  public PlayerNavigatorRecordSelectionListener(
      NavigatorPanel navigatorPanel, 
      IPlayerService playerService,
      PlayerFormFactory playerFormFactory) {
    super(navigatorPanel);
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _playerFormFactory = Objects.requireNonNull(playerFormFactory, "playerFormFactory may not be null");
  }

  @Override
  protected RomeoForm newForm(Object record) {
    return _playerFormFactory.newPlayerForm((IPlayer)record, false);
  }

  /**
   * Can handle both an IPlayer or a Map with a player id in it. In the later
   * case it will retreive the record to be passed from the service based on id
   * to0.
   */
  @Override
  public void recordSelected(Object record) {
    if(record instanceof PlayerImpl) {
      openRecord(record);
    } else if(record instanceof Map) {
      PlayerId id = null;
      Object idObj = Objects.requireNonNull( ((Map<?, ?>) record).get("id"), "player id in map may not be null");
      if(idObj instanceof String) {
        id = new PlayerId( (String)idObj );
      } else if (idObj instanceof PlayerId) {
        id = (PlayerId)idObj;
      } else {
        throw new ClassCastException("id object in player map is of type " + idObj.getClass().getName() );
      }      
      IPlayer player = _playerService.loadPlayer(id);
      openRecord(player);
    } else {
      throw new UnsupportedOperationException("Only PlayerImpl or player Map are supported here . Received:" + record);
    }
  }

}
