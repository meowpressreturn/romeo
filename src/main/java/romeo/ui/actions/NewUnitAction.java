package romeo.ui.actions;

import java.util.Objects;

import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.units.impl.UnitImpl;
import romeo.units.ui.UnitFormFactory;
import romeo.units.ui.UnitFormLogic;

public class NewUnitAction extends AbstractNewRecordAction {
  
  private final UnitFormFactory _unitFormFactory;
  
  public NewUnitAction(NavigatorPanel navigatorPanel, UnitFormFactory unitFormFactory) {
    super(navigatorPanel);
    
    _unitFormFactory = Objects.requireNonNull(unitFormFactory, "unitFormFactory may not be null");
    
    setDescription("Create a Unit record");
    setName("New Unit");
    setIcon("/images/unitNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return _unitFormFactory.createUnitForm();
  }

  @Override
  protected Object newRecord() {
    return new UnitImpl(null, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        UnitFormLogic.NEW_UNIT_ACRONYM_TO_CLEAR_WHEN_LOADING_IN_FORM, null);
  }
  
}
