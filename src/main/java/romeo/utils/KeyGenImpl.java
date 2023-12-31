
package romeo.utils;

import java.util.Locale;
import java.util.Random;

/**
 * Generator for unique string row ids in database. Note that this is only
 * suitable for a single machine environment and not for use in a clustered J2EE
 * environment or anything fancy like that.
 * n.b. this code was written back in 2005 to run on Java 1.3 or 1.4 when
 *      java.util.UUID wasn't around yet. Romeo dates back to 2006 but IIRC
 *      targeted 1.4 (?) so I just grabbed this code from some other project of
 *      mine and threw it in as a quick way to generate unique keys for rows :-)
 */
public class KeyGenImpl implements IKeyGen {
  private static final String CLASS_LOAD_TIME;
  static {
    String timeHex = Long.toHexString(System.currentTimeMillis() >>> 1);
    CLASS_LOAD_TIME = formatDigits(timeHex, 10);
  }
  private static int _runningNumber = 0;
  private static Random _random = new Random();

  /**
   * Creates and returns a key
   * @return key
   */
  @Override
  public synchronized String createIdKey() {
    StringBuffer key = new StringBuffer(24);
    key.append(CLASS_LOAD_TIME);
    key.append(formatDigits(Integer.toHexString((int) System.currentTimeMillis()), 5));
    key.append(formatDigits(Integer.toHexString(_random.nextInt()), 5));
    key.append(formatDigits(Integer.toHexString(_runningNumber++), 4));
    return key.toString().toUpperCase(Locale.US);
  }

  /**
   * Format the key to ensure it is the correct number of characters
   * @param unformatted
   * @param numberOfDigits
   */
  protected static String formatDigits(String unformatted, int numberOfDigits) {
    if(numberOfDigits <= unformatted.length()) {
      return unformatted.substring(unformatted.length() - numberOfDigits);
    } else {
      StringBuffer idKey = new StringBuffer(numberOfDigits);
      int zeros = numberOfDigits - unformatted.length();
      for(int i = 0; i < zeros; i++) {
        idKey.append("0");
      }
      idKey.append(unformatted);
      return idKey.toString();
    }
  }

}
