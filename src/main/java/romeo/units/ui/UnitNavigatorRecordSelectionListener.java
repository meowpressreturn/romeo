package romeo.units.ui;

import java.util.Objects;

import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;

public class UnitNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  private final UnitFormFactory _unitFormFactory;
  
  public UnitNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel, UnitFormFactory unitFormFactory) {
    super(navigatorPanel);
    _unitFormFactory = Objects.requireNonNull(unitFormFactory, "unitFormFactory may not be null");
  }

  @Override
  protected RomeoForm newForm() {
    return _unitFormFactory.createUnitForm();
  }

}
