//2008-12-10
package romeo.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Superclass for Romeo Action objects. This provides some error trapping and
 * display functionality.
 */
public abstract class AbstractRomeoAction extends AbstractAction {

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
      Log log = LogFactory.getLog(this.getClass());
      log.error("Error performing action", ex);
    }
  }
}
