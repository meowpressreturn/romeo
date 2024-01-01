package romeo.ui.actions;

import java.util.Objects;

import org.slf4j.Logger;

import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.impl.XFactorImpl;
import romeo.xfactors.ui.XFactorFormFactory;

public class NewXFactorAction extends AbstractNewRecordAction {
  
  private final XFactorFormFactory _xFactorFormFactory;
  
  public NewXFactorAction(
      Logger log,
      NavigatorPanel navigatorPanel, 
      XFactorFormFactory xFactorFormFactory) {
    super(log, navigatorPanel);
    
    _xFactorFormFactory = Objects.requireNonNull(xFactorFormFactory, "xFactorFormFactory may not be null");
    
    setDescription("Create an X-Factor record");
    setName("New X-Factor");
    setIcon("/images/xFactorNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return _xFactorFormFactory.newXFactorForm(newRecord(), true);
  }

  private IXFactor newRecord() {
    return new XFactorImpl();
  }
}
