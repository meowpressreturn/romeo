package romeo.units.ui;

import java.util.Objects;

import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.units.api.IUnit;

public class UnitNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  private final UnitFormFactory _unitFormFactory;
  
  public UnitNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel, UnitFormFactory unitFormFactory) {
    super(navigatorPanel);
    _unitFormFactory = Objects.requireNonNull(unitFormFactory, "unitFormFactory may not be null");
  }

  @Override
  protected RomeoForm newForm(Object record) {
    return _unitFormFactory.createUnitForm((IUnit)record, false);
  }
}
