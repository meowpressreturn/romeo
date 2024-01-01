package romeo.units.ui;

import java.util.Objects;

import org.slf4j.Logger;

import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.units.api.IUnit;

public class UnitNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  private final UnitFormFactory _unitFormFactory;
  
  public UnitNavigatorRecordSelectionListener(
      Logger log,
      NavigatorPanel navigatorPanel, 
      UnitFormFactory unitFormFactory) {
    super(log, navigatorPanel);
    _unitFormFactory = Objects.requireNonNull(unitFormFactory, "unitFormFactory may not be null");
  }

  @Override
  protected RomeoForm newForm(Object record) {
    return _unitFormFactory.createUnitForm((IUnit)record, false);
  }
}
