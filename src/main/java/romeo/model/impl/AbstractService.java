package romeo.model.impl;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;

import romeo.model.api.IService;
import romeo.model.api.IServiceListener;
import romeo.persistence.AbstractPersistenceService;

/**
 * Provides an implementation for adding and notifying listeners.
 * It is not a requirement that services extends this class but it may be useful for them to do so.
 * Most services will probably extend the subclass of this {@link AbstractPersistenceService} instead.
 */
public class AbstractService implements IService {
    
    protected final Logger _log;  
    protected List<IServiceListener> _listeners = new ArrayList<IServiceListener>();

    /**
     * Constructor
     * @param log logger, you should use the implementation class as the category
     */
    public AbstractService(Logger log) {
      _log = Objects.requireNonNull(log, "log may not be null");
    }

    /**
     * Add a listener to this service if it is not already in the list
     * @param listener
     */
    @Override
    public void addListener(IServiceListener listener) {
      if(!_listeners.contains(listener)) {
        _log.trace("Adding listener " + listener);
        _listeners.add(listener);
      }
    }

    /**
     * Remove a listener from this services listener list if it is in the list
     * @param listener
     */
    @Override
    public void removeListener(IServiceListener listener) {
      if(_listeners.contains(listener)) {
        _log.trace("Removing listener " + listener);
        _listeners.remove(listener);
      }
    }

    /**
     * Notify all service listeners. Any exceptions they throw will be logged but
     * will not prevent notification of the next service in the list. You may pass
     * null in which case a new EventObject will be created for the notification.
     * @param e
     *          event object to send
     */
    protected void notifyDataChanged(EventObject event) {
      Objects.requireNonNull(event, "event may not be null");
      _log.trace("notifyDataChanged: " + event);
      List<IServiceListener> safeCopy = new ArrayList<IServiceListener>(_listeners);
      for(Iterator<IServiceListener> i = safeCopy.iterator(); i.hasNext();) {
        IServiceListener listener = (IServiceListener) i.next();
        try {
          _log.trace("Notifying listener " + listener);
          listener.dataChanged(event);
        } catch(Exception e) {
          _log.error("Listener " + listener + " threw exception handling data change notification from " + this, e);
          throw new RuntimeException("Service listener threw exception:" + e.getMessage(),e);
        }
      }
    }

    protected void notifyDataChanged() {
      notifyDataChanged( new EventObject(this) );
    }
  }













