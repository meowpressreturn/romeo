//2008-12-08
package romeo;

import romeo.ui.ErrorDialog;

/**
 * Used to report exceptions for which a stacktrace is not desirable (such as
 * missing unit.csv for example). (It is the {@link ErrorDialog} (or other code)
 * that does the rendering however - the stacktrace returned by this class will
 * be normal).
 */
public class ApplicationException extends RuntimeException {
  /**
   * Constructor
   * @param message
   */
  public ApplicationException(String message) {
    super(message);
  }

  /**
   * Constructor
   * @param message
   * @param cause
   *          optional cause
   */
  public ApplicationException(String message, Exception cause) {
    super(message, cause);
  }
}
