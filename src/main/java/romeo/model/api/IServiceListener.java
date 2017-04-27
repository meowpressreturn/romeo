
package romeo.model.api;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Interface exposed by classes that wish to listen for change notification from
 * an {@link IService}.
 */
public interface IServiceListener extends EventListener {
  /**
   * Called by a service when data has changed.
   * @param service
   * @param event
   *          gives more details of the change
   */
  public void dataChanged(EventObject event);
}
