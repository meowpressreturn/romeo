package romeo.persistence;

import romeo.ApplicationException;

/**
 * Subclass of {@link ApplicationException} Thrown by services when they detect
 * an attempt to save a record with duplicate data for a field that is required
 * to be unique, such as name.
 */
public class DuplicateRecordException extends ApplicationException {
  
  /**
   * Constructor to which you pass a message that will (usually) be displayed to the end user in a dialog.
   * @param message
   */
  public DuplicateRecordException(String message) {
    super(message);
  }
  
}



















