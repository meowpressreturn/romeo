//2008-12-10
package romeo.ui;

import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.AbstractAction;

import org.slf4j.Logger;

/**
 * Superclass for Romeo Action objects. This provides some error trapping and
 * display functionality.
 */
public abstract class AbstractRomeoAction extends AbstractAction {
  
  protected final Logger _log;
  
  /**
   * Constructor
   * @param log you should use the concrete subclasses category for logger
   */
  public AbstractRomeoAction(Logger log) {
    _log = Objects.requireNonNull(log,  "log may not be null");
  }

  protected abstract void doActionPerformed(ActionEvent e);
  

  /**
   * Calls doActionPerformed(e) catching any exceptions thrown and reporting
   * them in an {@link ErrorDialog} and logging them.
   * @param e
   */
  @Override
  public final void actionPerformed(ActionEvent e) {
    try {
      doActionPerformed(e);
    } catch(Exception ex) {
      ErrorDialog dialog = new ErrorDialog("Internal Error", ex, false);
      dialog.show();
      _log.error("Error performing action", ex);
    }
  }
}
