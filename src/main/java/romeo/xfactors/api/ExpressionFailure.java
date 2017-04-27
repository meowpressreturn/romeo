//2008-12-09
package romeo.xfactors.api;

import romeo.xfactors.expressions.Fail;

/**
 * Exception raised by the {@link Fail} expression
 */
public class ExpressionFailure extends RuntimeException {
  /**
   * Constructor
   * @param msg
   * @param cause
   */
  public ExpressionFailure(String msg, Exception cause) {
    super(msg, cause);
  }
}
