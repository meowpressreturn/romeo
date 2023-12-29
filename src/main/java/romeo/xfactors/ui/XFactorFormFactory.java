package romeo.xfactors.ui;

import java.util.Objects;

import romeo.xfactors.api.IXFactorService;

public class XFactorFormFactory {
  
  private final IXFactorService _xFactorService;
  
  public XFactorFormFactory(IXFactorService xFactorService) {
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
  }

  public XFactorForm newXFactorForm() {
    return new XFactorForm(_xFactorService);
  }
  
}
