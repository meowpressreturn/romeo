/*
 * NoSuchXFactorException.java
 * Created on Mar 15, 2006
 */
package romeo.xfactors.api;

/**
 * This exception is thrown by the xfactor compiler if it cannot find a named
 * xfactor
 */
public class NoSuchXFactorException extends RuntimeException {
  /**
   * Constructor
   * @param message
   */
  public NoSuchXFactorException(String message) {
    super(message);
  }

}
