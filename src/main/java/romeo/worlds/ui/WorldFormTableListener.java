package romeo.worlds.ui;

import java.util.Objects;

import romeo.Romeo;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.api.IHistory;

public class WorldFormTableListener extends AbstractNavigatorRecordSelectionListener {
  protected IPlayerService _playerService;

  public WorldFormTableListener(NavigatorPanel navigatorPanel, IPlayerService playerService) {
    super(navigatorPanel);
    _playerService = Objects.requireNonNull(playerService);
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createPlayerForm();
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
