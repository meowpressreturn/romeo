package romeo.xfactors.ui;

import java.util.List;

import romeo.ui.forms.FieldDef;
import romeo.ui.forms.IFormLogic;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.RomeoFormInitialiser;

public class XFactorForm extends RomeoForm {
  
  public XFactorForm(
      RomeoFormInitialiser initialiser,
      List<FieldDef> fields,
      IFormLogic logic,
      Object record,
      boolean isNewRecord) {
    super(initialiser, fields, logic, false, record, isNewRecord);    
    setName("X-Factor");    
  }
}
