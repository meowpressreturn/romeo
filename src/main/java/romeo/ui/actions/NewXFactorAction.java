package romeo.ui.actions;

import java.util.Objects;

import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.xfactors.impl.XFactorImpl;
import romeo.xfactors.ui.XFactorFormFactory;

public class NewXFactorAction extends AbstractNewRecordAction {
  
  private final XFactorFormFactory _xFactorFormFactory;
  
  public NewXFactorAction(NavigatorPanel navigatorPanel, XFactorFormFactory xFactorFormFactory) {
    super(navigatorPanel);
    
    _xFactorFormFactory = Objects.requireNonNull(xFactorFormFactory, "xFactorFormFactory may not be null");
    
    setDescription("Create an X-Factor record");
    setName("New X-Factor");
    setIcon("/images/xFactorNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return _xFactorFormFactory.newXFactorForm();
  }

  @Override
  protected Object newRecord() {
    return new XFactorImpl();
  }
}
