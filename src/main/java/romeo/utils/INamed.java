/*
 * INamed.java
 * Created on Mar 1, 2006
 */
package romeo.utils;

/**
 * Interface for object that has a name property with a getName() method (Things
 * like players and worlds and units have names that we would wish to show in
 * the UI etc...)
 */
public interface INamed {
  /**
   * Return the name
   * @return name
   */
  public String getName();
}
