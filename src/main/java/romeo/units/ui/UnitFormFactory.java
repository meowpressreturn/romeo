package romeo.units.ui;

import java.util.Objects;

import romeo.ui.forms.RomeoForm;
import romeo.units.api.IUnitService;
import romeo.xfactors.api.IXFactorService;

public class UnitFormFactory {
  
  private final IUnitService _unitService;
  private final IXFactorService _xFactorService;
  
  public UnitFormFactory(IUnitService unitService, IXFactorService xFactorService) {
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
  }
  
  public RomeoForm createUnitForm() {
    return new UnitForm(_unitService, _xFactorService);
  }
}
