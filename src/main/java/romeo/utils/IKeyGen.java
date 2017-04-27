package romeo.utils;

/**
 * Public interface of the key generator used to generate keys for db records
 */
public interface IKeyGen {
  /**
   * Return a key
   * @param idKey
   */
  public String createIdKey();
}
