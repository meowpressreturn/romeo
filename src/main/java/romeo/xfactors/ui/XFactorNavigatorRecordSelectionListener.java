package romeo.xfactors.ui;

import romeo.Romeo;
import romeo.ui.NavigatorPanel;
import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.forms.RomeoForm;

public class XFactorNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  public XFactorNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createXFactorForm();
  }

}
