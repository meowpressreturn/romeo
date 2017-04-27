/*
 * IValidatingField.java
 * Created on Feb 6, 2006
 */
package romeo.ui.forms;

/**
 * Interface for fields that validate themselves
 */
public interface IValidatingField {
  /**
   * Returns true if the field value is valid
   * @return valid
   */
  public boolean isFieldValid();
}
