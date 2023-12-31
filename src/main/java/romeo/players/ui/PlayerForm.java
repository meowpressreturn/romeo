package romeo.players.ui;

import java.util.List;

import romeo.players.api.IPlayer;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.RomeoFormInitialiser;

public class PlayerForm extends RomeoForm {
  
  protected PlayerForm(RomeoFormInitialiser initialiser,
      List<FieldDef> fields,
      PlayerFormLogic logic,
      IPlayer record,
      boolean isNewRecord) {
    super(initialiser, fields, logic, false, record, isNewRecord);
    setName("Player");
  }
}