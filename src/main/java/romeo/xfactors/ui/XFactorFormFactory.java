package romeo.xfactors.ui;

import java.util.Objects;

import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IXFactorService;

public class XFactorFormFactory {
  
  private final IXFactorService _xFactorService;
  private final IExpressionParser _expressionParser;
  
  public XFactorFormFactory(IXFactorService xFactorService, IExpressionParser expressionParser) {
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
    _expressionParser = Objects.requireNonNull(expressionParser, "expressionParser may not be null");
  }

  public XFactorForm newXFactorForm() {
    return new XFactorForm(_xFactorService, _expressionParser);
  }
  
}
