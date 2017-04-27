/*
 * IExpression.java
 * Created on Mar 12, 2006
 */
package romeo.xfactors.api;

import romeo.battle.impl.RoundContext;

/**
 * Common interface shared by all xfactor expression objects. (Most of these
 * objects will themselves store other IExpressions which provide values for use
 * in their logics, however such linkage is not presented via this interface
 * which make an entire expression subtree appear as a black box. When an
 * expression is evaluated it may require access to contextual data (for example
 * things like round number, counts of other units and so forth). This is
 * provided via the RoundContext object that is passed to the evaluate method.
 */
public interface IExpression {
  /**
   * Evaluate the expression in the context of the round and return the result.
   * @param context
   *          The RoundContext object that provides exoression logic access to
   *          context info
   * @return result
   */
  public Object evaluate(RoundContext context);
}
