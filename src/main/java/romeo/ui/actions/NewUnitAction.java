package romeo.ui.actions;

import romeo.Romeo;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.units.impl.UnitImpl;

public class NewUnitAction extends AbstractNewRecordAction {
  public NewUnitAction(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
    setDescription("Create a Unit record");
    setName("New Unit");
    setIcon("/images/unitNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createUnitForm();
  }

  @Override
  protected Object newRecord() {
    return new UnitImpl(null, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", null);
  }
}
