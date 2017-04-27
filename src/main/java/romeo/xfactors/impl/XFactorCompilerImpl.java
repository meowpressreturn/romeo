package romeo.xfactors.impl;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import romeo.model.api.IServiceListener;
import romeo.xfactors.api.CompiledXFactor;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorCompiler;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.api.NoSuchXFactorException;
import romeo.xfactors.api.XFactorId;

/**
 * Implementation of the XFactorCompiler. It uses an IExpressionParser to
 * perform the hard work of interpreting the expression text for each individual
 * xf expression.
 */
public class XFactorCompilerImpl implements IXFactorCompiler, IServiceListener {
  
  protected IExpressionParser _parser;
  protected IXFactorService _xfactorService;
  protected Map<XFactorId, CompiledXFactor> _compiledXfactors = new HashMap<>();

  /**
   * Constructor. An xfactor parser and the xfactor service reference are
   * required. (We would generally wire all this up using spring DI. See
   * context.xml).
   * @param parser
   * @param service
   */
  public XFactorCompilerImpl(IExpressionParser parser, IXFactorService xfactorService) {
    _parser = parser;
    _xfactorService = xfactorService;
    xfactorService.addListener(this);
  }

  /**
   * Compile the specified XFactor. This method is synchronised as it will be
   * called from a different thread to the UI (that of the battle simulator).
   * @param xfactor
   *          the xfactor bean that holds the XFEL
   * @return compiledXfactor bean holding the compiled IExpressions
   */
  @Override
  public synchronized CompiledXFactor compile(IXFactor xFactor) {
    if(xFactor == null) {
      return null;
    }
    try {
      CompiledXFactor compiled = new CompiledXFactor();
      IExpression trigger = _parser.getExpression(xFactor.getTrigger());
      IExpression xfAttacks = _parser.getExpression(xFactor.getXfAttacks());
      IExpression xfOffense = _parser.getExpression(xFactor.getXfOffense());
      IExpression xfDefense = _parser.getExpression(xFactor.getXfDefense());
      IExpression xfPd = _parser.getExpression(xFactor.getXfPd());
      IExpression xfRemove = _parser.getExpression(xFactor.getXfRemove());
      compiled.setTrigger(trigger);
      compiled.setXfAttacks(xfAttacks);
      compiled.setXfOffense(xfOffense);
      compiled.setXfDefense(xfDefense);
      compiled.setXfPd(xfPd);
      compiled.setXfRemove(xfRemove);
      return compiled;
    } catch(Exception e) {
      throw new RuntimeException("Unable to compile xFactor:" + xFactor, e);
    }
  }

  /**
   * Will retrieve the named XFactor from the XFactor service and compile it,
   * caching a copy of the compiled version in its map. Future requests for that
   * x factor will return the compiled one from the cache. The cache is cleared
   * if a dataChanged message is received from the XFactorService. If the named
   * factor is not found then an exception is raised.
   * @param id
   *          The id of the xFactor 
   * @return compiledXFactor
   * @throws NoSuchXFactorException
   *           if the named factor doesnt exist
   */
  @Override
  public synchronized CompiledXFactor getXFactor(XFactorId id) {
    CompiledXFactor cxf = _compiledXfactors.get(id);
    if(cxf == null) {
      IXFactor xf = _xfactorService.getXFactor(id);
      if(xf == null) {
        throw new NoSuchXFactorException(id.toString());
      }
      cxf = compile(xf);
      _compiledXfactors.put(id, cxf);
    }
    return cxf;
  }

  /**
   * Listener callback. Will clear the tree of compiled xfactors if any xfactor
   * is modified. This method is synchronized as the battle simulator may also
   * be in operation. Changing the xfactors while the simulator is running isnt
   * a great idea though.
   * @param event
   */
  @Override
  public synchronized void dataChanged(EventObject event) {
    _compiledXfactors = new HashMap<XFactorId, CompiledXFactor>();
  }

}
