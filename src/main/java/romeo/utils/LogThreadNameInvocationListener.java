package romeo.utils;

import java.util.EventObject;

import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceListener;

/**
 * Service listener that just logs (at debug level) the name of the thread it was called on.
 */
public class LogThreadNameInvocationListener implements IServiceListener {

  @Override
  public void dataChanged(EventObject event) {
    Object source = event==null ? null : event.getSource();
    if(source==null) {
      source = "unknown";
    }
    Thread currentThread = Thread.currentThread();
    LogFactory.getLog(this.getClass()).debug("dataChanged() called on thread " + currentThread.getName() + " by " + source);
    
  }

}



















