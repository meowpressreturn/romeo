package romeo.utils;

import java.util.EventObject;
import java.util.Objects;

import org.slf4j.Logger;

import romeo.model.api.IServiceListener;

/**
 * Service listener that just logs (at debug level) the name of the thread it was called on.
 */
public class LogThreadNameInvocationListener implements IServiceListener {
  
  private final Logger _log;
  
  public LogThreadNameInvocationListener(Logger log) {
    _log = Objects.requireNonNull(log, "log may not be null");
  }

  @Override
  public void dataChanged(EventObject event) {
    Object source = event==null ? null : event.getSource();
    if(source==null) {
      source = "unknown";
    }
    Thread currentThread = Thread.currentThread();
    _log.debug("dataChanged() called on thread " + currentThread.getName() + " by " + source);  
  }
}



















