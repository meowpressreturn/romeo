package romeo.utils.events;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the IEventHub interface. This implementation is not synchronized.
 */
public class EventHubImpl implements IEventHub {
  //The listener list may cotain either raw IEventListener (for strong references) or Reference
  //for the WeakReferences. (Java doesn't provide a StrongReference directly, not let us subclass
  //Reference to create our own.)
  protected List<Object> _listeners;

  public EventHubImpl() {
    _listeners = new LinkedList<Object>();
  }

  @Override
  public void addListener(IEventListener listener) {
    cleanup();
    Objects.requireNonNull(listener, "listener must not be null");
    if(!hasListener(listener)) {
      _listeners.add(listener);
    }
  }

  @Override
  public void addWeakListener(IEventListener listener) {
    cleanup();
    Objects.requireNonNull(listener, "listener must not be null");
    if(!hasListener(listener)) {
      WeakReference<IEventListener> ref = new WeakReference<IEventListener>(listener);
      _listeners.add(ref);
    }
  }

  @Override
  public void removeListener(IEventListener listener) {
    ListIterator<Object> li = _listeners.listIterator();
    while(li.hasNext()) {
      Object listEntry = li.next();
      IEventListener entry = listenerReference(listEntry);
      if(entry == listener) {
        li.remove();
        return;
      }
    }
  }

  @SuppressWarnings("rawtypes")
  protected void cleanup() {
    ListIterator<Object> li = _listeners.listIterator();
    while(li.hasNext()) {
      Object o = li.next();
      if(o instanceof Reference && ((Reference) o).get() == null) {
        li.remove();
      }
    }
  }

  /**
   * Returns true if this listener is already registered
   * @param listener
   * @return hasListener
   */
  public boolean hasListener(IEventListener listener) {
    for(Object listEntry : _listeners) {
      if(listenerReference(listEntry) == listener) {
        return true;
      }
    }
    return false;
  }

  /**
   * Fires the event to the onEvent method of all the listeners in the listerner
   * list. Internally this implementation makes its own copy of the list first
   * to allow listeners to more safely modify the list if they wish.
   * @param event
   *          this may not be null
   */
  @Override
  public void notifyListeners(EventObject event) {
    Objects.requireNonNull(event, "event must not be null");
    Log log = LogFactory.getLog(this.getClass());
    log.debug("Notifying listeners of event: " + event);
    for(Object listEntry : _listeners.toArray()) {
      IEventListener listener = listenerReference(listEntry);
      if(listener != null) {
        log.debug("Notifying " + listener.getClass().getName());
        listener.onEvent(event);
      }
      if(listener == null) {
        //Remove dead reference - happens when a weak reference had been dropped
        log.debug("Removing a dead listener from the list");
        _listeners.remove(listEntry);
      }
    }
  }

  /**
   * Internal utility method to obtain the IEventListener reference from an
   * entry in the listener list.
   * @param listEntry
   *          the entry in the list. Null is not permitted
   * @return eventListener the listener - may be null if a Reference target was
   *         garbage collected
   */
  @SuppressWarnings("unchecked")
  protected IEventListener listenerReference(Object listEntry) {
    Objects.requireNonNull(listEntry, "listEntry must not be null");
    if(listEntry instanceof Reference) {
      listEntry = ((Reference<IEventListener>) listEntry).get();
    }
    if(listEntry == null) {
      return null;
    }
    if(listEntry instanceof IEventListener) {
      return (IEventListener) listEntry;
    }
    throw new ClassCastException("Expected an IEventListener or Reference for listEntry, but found an instance of "
        + listEntry.getClass().getName());
  }
}
