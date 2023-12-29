package romeo.players.ui;

import java.util.Objects;

import romeo.players.api.IPlayerService;
import romeo.settings.api.ISettingsService;
import romeo.worlds.api.IWorldService;

public class PlayerFormFactory {
  
  private final IPlayerService _playerService;
  private final IWorldService _worldService;
  private final ISettingsService _settingsService;
  
  public PlayerFormFactory(IPlayerService playerService, IWorldService worldService, ISettingsService settingsService) {
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
  }
  
  public PlayerForm newPlayerForm() {
    return new PlayerForm(_playerService, _worldService, _settingsService);
  }
}
