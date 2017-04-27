package romeo.ui.actions;

import romeo.Romeo;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.xfactors.impl.XFactorImpl;

public class NewXFactorAction extends AbstractNewRecordAction {
  public NewXFactorAction(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
    setDescription("Create an X-Factor record");
    setName("New X-Factor");
    setIcon("/images/xFactorNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createXFactorForm();
  }

  @Override
  protected Object newRecord() {
    return new XFactorImpl();
  }
}
