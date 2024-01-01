package romeo.importdata.impl;

import java.util.Objects;

import org.slf4j.LoggerFactory;

import romeo.importdata.IWorldImporter;
import romeo.players.api.IPlayerService;
import romeo.settings.api.ISettingsService;
import romeo.worlds.api.IWorldService;

/**
 * Instances of WorldImporter have state so you need to create a new one for each import.
 * This requires supplying a number of dependencies. 
 * This class holds references to those dependencies and uses them to create instances on demand.
 */
public class WorldImporterFactory {
  private final IWorldService _worldService;
  private final IPlayerService _playerService;
  private final ISettingsService _settingsService;
  
  public WorldImporterFactory(
      IWorldService worldService,
      IPlayerService playerService,
      ISettingsService settingsService) {
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
  }
  
  public IWorldImporter newInstance() {
    return new WorldImporterImpl(LoggerFactory.getLogger(WorldImporterImpl.class), _worldService, _playerService, _settingsService);
  }
}
