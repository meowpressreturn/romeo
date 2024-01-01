package romeo.battle;

import java.util.Objects;

import org.slf4j.LoggerFactory;

import romeo.battle.impl.BattleCalculatorImpl;
import romeo.xfactors.api.IXFactorCompiler;

/**
 * Instances of BattleCalculatorImpl are not singletons so we need to create a new one for each
 * battle. This class will do that while hiding details of what dependencies it needs from client
 * classes.
 */
public class BattleCalculatorFactory {
  
  private final IXFactorCompiler _xfactorCompiler;
  
  public BattleCalculatorFactory(IXFactorCompiler xfactorCompiler) {
    _xfactorCompiler = Objects.requireNonNull(xfactorCompiler, "xfactorcompiler may not be null");
  }
  
  public IBattleCalculator newBattleCalculator() {
    return new BattleCalculatorImpl(
        LoggerFactory.getLogger(BattleCalculatorImpl.class), 
        _xfactorCompiler);
  }

}
