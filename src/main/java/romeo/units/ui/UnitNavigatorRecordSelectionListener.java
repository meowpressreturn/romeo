package romeo.units.ui;

import romeo.Romeo;
import romeo.ui.NavigatorPanel;
import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.forms.RomeoForm;

public class UnitNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  public UnitNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createUnitForm();
  }

}
