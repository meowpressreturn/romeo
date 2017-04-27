/*
 * XFactorCompiler.java
 * Created on Mar 13, 2006
 */
package romeo.xfactors.api;

/**
 * The public interface for the xfactor compiler. The compiler is used to
 * convert the xfactor expression text into the IExpression object trees that
 * contain the logic to evaluate the expressions at runtime.
 */
public interface IXFactorCompiler {
  /**
   * Given an XFactor definition, compile it.
   * @param xfactor
   * @return compiledXfactor
   */
  public CompiledXFactor compile(IXFactor xFactor);

  /**
   * Lookup the xfactor definition by its name and compile it.
   * @param id
   * @return compiledXf
   */
  public CompiledXFactor getXFactor(XFactorId id);
}
