
package romeo.utils;

import java.util.Locale;
import java.util.Random;

/**
 * Generator for unique string row ids in database. Note that this is onlsy
 * suitable for a single machine environment and not for use in a clustered J2EE
 * environment or anything fancy like that.
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
