package romeo.worlds.ui;

import java.util.Objects;

import romeo.players.api.IPlayerService;
import romeo.players.ui.PlayerFormFactory;
import romeo.settings.api.ISettingsService;
import romeo.units.api.IUnitService;
import romeo.worlds.api.IWorldService;

/**
 * Keeps a reference to the dependencies needed to create a WorldForm and
 * uses them to create instances on demand
 */
public class WorldFormFactory {

  private final IWorldService _worldService;
  private final ISettingsService _settingsService;
  private final IPlayerService _playerService;
  private final IUnitService _unitService;
  private final PlayerFormFactory _playerFormFactory;
    
  public WorldFormFactory(
      IWorldService worldService, 
      ISettingsService settingsService,
      IPlayerService playerService,
      IUnitService unitService,
      PlayerFormFactory playerFormFactory) {
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _playerFormFactory = Objects.requireNonNull(playerFormFactory, "playerFormFactory may not be null");
  }
  
  public WorldForm newWorldForm() {
    return new WorldForm(_worldService, _settingsService, _playerService, _unitService, _playerFormFactory);
  }
}
