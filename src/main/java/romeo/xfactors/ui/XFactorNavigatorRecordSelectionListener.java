package romeo.xfactors.ui;

import java.util.Objects;

import org.slf4j.Logger;

import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.xfactors.api.IXFactor;

public class XFactorNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  private final XFactorFormFactory _xFactorFormFactory;
  
  public XFactorNavigatorRecordSelectionListener(
      Logger log,
      NavigatorPanel navigatorPanel, 
      XFactorFormFactory xFactorFormFactory) {
    super(log, navigatorPanel);
    _xFactorFormFactory = Objects.requireNonNull(xFactorFormFactory, "xFactorFormFactory may not be null");
  }

  @Override
  protected RomeoForm newForm(Object record) {
    return _xFactorFormFactory.newXFactorForm((IXFactor)record, false);
  }

}
