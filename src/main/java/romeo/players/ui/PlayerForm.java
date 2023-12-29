package romeo.players.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import romeo.players.api.IPlayerService;
import romeo.settings.api.ISettingsService;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.api.IWorldService;

public class PlayerForm extends RomeoForm {
  
  public PlayerForm(IPlayerService playerService, IWorldService worldService, ISettingsService settingsService) {
    Objects.requireNonNull(playerService, "playerService may not be null");
    Objects.requireNonNull(worldService, "worldService may not be null");
    Objects.requireNonNull(settingsService, "settingsService may not be null");
    
    setName("Player");
    setFormLogic(new PlayerFormLogic(playerService, worldService, settingsService));
    List<FieldDef> fields = new ArrayList<FieldDef>();
    
    //name
    FieldDef name = new FieldDef("name","Name");
    name.setMandatory(true);
    fields.add(name);
    
    //color
    FieldDef color = new FieldDef("color","Colour", FieldDef.TYPE_COLOR);
    color.setDefaultValue("255,0,0");
    fields.add(color);
    
    //status
    fields.add(new FieldDef("status","Status"));
    
    //team
    fields.add(new FieldDef("team","Team"));
    
    //notes
    FieldDef notes = new FieldDef("notes","Notes", FieldDef.TYPE_LONG_TEXT);
    notes.setWide(true);
    fields.add(notes);
    
    //turn
    FieldDef turn = new FieldDef("turn","Turn", FieldDef.TYPE_LABEL);
    turn.setWide(true);
    fields.add(turn);
    
    //totalFirepower
    fields.add(new FieldDef("totalFirepower","Firepower", FieldDef.TYPE_LABEL));
    
    //worldCount
    fields.add(new FieldDef("worldCount","Worlds", FieldDef.TYPE_LABEL));
    
    //totalLabour
    fields.add(new FieldDef("totalLabour","Labour", FieldDef.TYPE_LABEL));
    
    //totalCapital
    fields.add(new FieldDef("totalCapital", "Capital", FieldDef.TYPE_LABEL));
    
    setFields(fields);
  }

}
