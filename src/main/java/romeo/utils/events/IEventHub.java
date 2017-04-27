package romeo.utils.events;

import java.util.EventObject;

/**
 * Object to which listeners can be added to receive notification of various
 * events. Implementations are expected to support both weak and strong listener
 * references.
 */
public interface IEventHub {
  /**
   * Register the listener. A strong reference is held, meaning that if there
   * are not other references to the listener somewhere it will still not be
   * eligible for garbage collection. If the listener is already registered
   * (either weak or strong) it will not be re-registered a second time.
   * @param listener
   */
  public void addListener(IEventListener listener);

  /**
   * Register the listener using a weak reference. As such, if there are no
   * other references held to the specified listener somewhere, then it will be
   * eligible for garbage collection. If the listener is already registered
   * (either weak or strong) it will not be re-registered a second time.
   * @param listener
   */
  public void addWeakListener(IEventListener listener);

  /**
   * Unregister the specified listener. If the listener is not registered to
   * begin with then the method will have no effect and will not raise an error.
   * @param listener
   */
  public void removeListener(IEventListener listener);

  /**
   * Inform all the registered listeners of the event. It is safe for the
   * listeners to add or remove listeners as part of their event handling, but
   * this shall not affect which listeners are informed of this event.
   * @param event
   */
  public void notifyListeners(EventObject event);
}
