package romeo.worlds.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import romeo.players.api.IPlayerService;
import romeo.players.ui.PlayerFormFactory;
import romeo.settings.api.ISettingsService;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoFormInitialiser;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;

/**
 * Keeps a reference to the dependencies needed to create a WorldForm and
 * uses them to create instances on demand
 */
public class WorldFormFactory {

  private final RomeoFormInitialiser _initialiser;
  private final IWorldService _worldService;
  private final ISettingsService _settingsService;
  private final IPlayerService _playerService;
  private final PlayerFormFactory _playerFormFactory;
    
  public WorldFormFactory(
      RomeoFormInitialiser initialiser,
      IWorldService worldService, 
      ISettingsService settingsService,
      IPlayerService playerService,
      PlayerFormFactory playerFormFactory) {
    _initialiser = Objects.requireNonNull(initialiser, "initialiser may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService may not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _playerFormFactory = Objects.requireNonNull(playerFormFactory, "playerFormFactory may not be null");
  }
  
  public WorldForm newWorldForm(IWorld record, boolean isNewRecord) {
    return new WorldForm(_initialiser, fields(), logic(), record, isNewRecord);
  }
  
  private WorldFormLogic logic() {
    return new WorldFormLogic(_worldService, _settingsService, _playerService, _playerFormFactory);
  }
  
  private List<FieldDef> fields() {
    ArrayList<FieldDef> fields = new ArrayList<FieldDef>();
    FieldDef nameDef = new FieldDef("name", "Name");
    nameDef.setMandatory(true);
    fields.add(nameDef);
    fields.add(new FieldDef("empty0", "", FieldDef.TYPE_FILLER));
    fields.add(new FieldDef("worldX", "World X", FieldDef.TYPE_INT));
    fields.add(new FieldDef("worldY", "World Y", FieldDef.TYPE_INT));
    fields.add(new FieldDef("worldEi", "EI", FieldDef.TYPE_INT));
    fields.add(new FieldDef("worldRer", "RER", FieldDef.TYPE_INT));
    fields.add(new FieldDef("scanner", "Scanner", FieldDef.TYPE_SCANNER_COMBO));
    fields.add(new FieldDef("notes", "Notes", FieldDef.TYPE_LONG_TEXT));

    fields.add(new FieldDef("empty1", "", FieldDef.TYPE_FILLER));
    fields.add(new FieldDef("empty2", "", FieldDef.TYPE_FILLER));

    //History values for the current turn
    fields.add(new FieldDef("turnLabel", "Turn", FieldDef.TYPE_LABEL));
    fields.add(new FieldDef("empty3", "", FieldDef.TYPE_FILLER));
    fields.add(new FieldDef("owner", "Owner"));
    fields.add(new FieldDef("firepower", "Firepower", FieldDef.TYPE_DOUBLE));
    fields.add(new FieldDef("labour", "Labour", FieldDef.TYPE_INT));
    fields.add(new FieldDef("capital", "Capital", FieldDef.TYPE_INT));

    //History display for the world
    fields.add(new FieldDef("history", "History", FieldDef.TYPE_CUSTOM));
    
    return fields;
  }
}
