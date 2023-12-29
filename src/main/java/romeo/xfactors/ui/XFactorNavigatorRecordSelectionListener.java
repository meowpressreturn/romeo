package romeo.xfactors.ui;

import java.util.Objects;

import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;

public class XFactorNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  private final XFactorFormFactory _xFactorFormFactory;
  
  public XFactorNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel, XFactorFormFactory xFactorFormFactory) {
    super(navigatorPanel);
    _xFactorFormFactory = Objects.requireNonNull(xFactorFormFactory, "xFactorFormFactory may not be null");
  }

  @Override
  protected RomeoForm newForm() {
    return _xFactorFormFactory.newXFactorForm();
  }

}
