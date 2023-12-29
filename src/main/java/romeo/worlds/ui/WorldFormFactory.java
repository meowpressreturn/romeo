package romeo.worlds.ui;

import java.util.Objects;

import romeo.settings.api.ISettingsService;
import romeo.worlds.api.IWorldService;

/**
 * Keeps a reference to the dependencies needed to create a WorldForm and
 * uses them to create instances on demand
 */
public class WorldFormFactory {

  private final IWorldService _worldService;
  private final ISettingsService _settingsService;
  
  public WorldFormFactory(IWorldService worldService, ISettingsService settingsService) {
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
  }
  
  public WorldForm newWorldForm() {
    return new WorldForm(_worldService, _settingsService);
  }
}
