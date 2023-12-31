package romeo.worlds.ui;

import java.util.Objects;

import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.ui.PlayerFormFactory;
import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.api.IHistory;

public class WorldFormHistoryTableListener extends AbstractNavigatorRecordSelectionListener {
  
  private final IPlayerService _playerService;
  private final PlayerFormFactory _playerFormFactory;

  public WorldFormHistoryTableListener(
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

  @Override
  public void recordSelected(Object record) {
    IHistory history = (IHistory) record;
    String owner = history.getOwner();
    IPlayer player = _playerService.loadPlayerByName(owner);
    if(player != null) {
      super.recordSelected(player);
    }
  }

}
