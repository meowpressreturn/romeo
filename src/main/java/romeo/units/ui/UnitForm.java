package romeo.units.ui;

import java.util.List;

import org.slf4j.Logger;

import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.RomeoFormInitialiser;
import romeo.units.api.IUnit;

public class UnitForm extends RomeoForm {

  protected UnitForm(
      Logger log,
      RomeoFormInitialiser initialiser, 
      List<FieldDef> fields, 
      UnitFormLogic logic, 
      IUnit record,
      boolean isNewRecord) {
    super(log, initialiser, fields, logic, false, record, isNewRecord);
    setName("Unit");
  }
}
