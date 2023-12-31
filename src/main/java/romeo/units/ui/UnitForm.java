package romeo.units.ui;

import java.util.List;

import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.RomeoFormInitialiser;
import romeo.units.api.IUnit;

public class UnitForm extends RomeoForm {

  protected UnitForm(
      RomeoFormInitialiser initialiser, 
      List<FieldDef> fields, 
      UnitFormLogic logic, 
      IUnit record,
      boolean isNewRecord) {
    super(initialiser, fields, logic, false, record, isNewRecord);
    setName("Unit");
  }
}
