package romeo.persistence;

/**
 * Interface that Services (etc) can implement to indicate that they can be informed when the
 * data they manage has been changed externally.
 */
public interface IExternalDataChangeListener {
  
  /**
   * Notify the listener that the data it manages was changed externally to it and that
   * cached data it holds may thus be stale.
   */
  public void dataChangedExternally();
  
}



















