
package romeo.model.api;

/**
 * Services are the things that provide persistence services for the various
 * Romeo entities. It would be more accurate to term them repositories, but they
 * are also a place to dump additional functionality that goes beyond just CRUD.
 * 
 * Services are required to provide a primitive listener-based notification
 * method to allow the UI code and other code that needs it a way to detect when
 * persisted data has been changed by some other code and take the necessary
 * action to update themselves to reflect or use the new values. The
 * implementation is not sophisticated in that the notifications often don't
 * specify details of what changed, only that something changed, or rather,
 * might have been changed.
 */
public interface IService {
  /**
   * Add a listener that will be informed when a change occurs to the data that
   * the service is responsible for.
   * @param listener
   */
  public void addListener(IServiceListener listener);

  /**
   * Remove the specified listener
   * @param listener
   */
  public void removeListener(IServiceListener listener);
}
