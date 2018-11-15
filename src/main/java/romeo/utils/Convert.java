package romeo.utils;

import java.awt.Color;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

import org.jfree.data.KeyedValues;

/**
 * Some utility methods for performing various conversions and checks for data
 * and datatypes
 */
public class Convert {
  /**
   * Find and return the nested rootCause exception for a chain of exceptions or
   * throwables
   * @param t
   * @return root
   */
  public static Throwable rootCause(Throwable t) {
    if(t == null) {
      return null;
    }
    Throwable cause = t.getCause();
    if(cause != null && cause != t) {
      return rootCause(cause);
    } else {
      return t;
    }
  }

  /**
   * Returns true if all the values in the collection are of a class that is
   * assignable from the required type. If allowNull is true then null values
   * will be ignored.
   * @param values
   * @param requiredClass
   * @param allowNull
   * @return allOfClass
   */
  public static boolean isAllClass(Collection<?> values, Class<?> requiredClass, boolean allowNull) {
    Objects.requireNonNull(values, "values may not be null");
    Objects.requireNonNull(requiredClass, "requiredClass may not be null");
    for(Object o : values) {
      if(o == null) {
        if(!allowNull) {
          return false;
        }
      } else {
        if(!o.getClass().isAssignableFrom(requiredClass)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Given a List will iterate it and remove any instances of null that are
   * encountered. (Obviously the list must be modifiable)
   * @param list 
   */
  public static void removeNulls(List<? extends Object> list) {
    Objects.requireNonNull(list, "list must not be null");
    ListIterator<? extends Object> i = list.listIterator();
    while(i.hasNext()) {
      Object value = i.next();
      if(value == null) {
        i.remove();
      }
    }
  }

  /**
   * Given an array of item container sizes and an item index will return the
   * index in the size array that the element would fall into. The item index
   * must be in the range 0..SUM(sizes)-1 For example if sizes = [5,5,5] and
   * element is 11 will return 2. (Romeo uses this in battle simulations to
   * allocate hits)
   * @param sizes
   * @param item
   * @return index
   */
  public static int getIndexOfItem(int sizes[], int item) {
    Objects.requireNonNull(sizes, "sizes may not be null");
    int counter = 0;
    for(int i = 0; i <= sizes.length; i++) {
      int qty = (int) sizes[i];
      int offsetIndex = item - counter;
      if(offsetIndex < qty) {
        return i;
      }
      counter += qty;
    }
    throw new IndexOutOfBoundsException("Out of range item index:" + item);
  }

  /**
   * Split a string containing specified delimiters into an array of String
   * using a StringTokenizer.
   * @param string
   * @param delimiters each charater in the string is a delimiter (as per StringTokenizer)
   */
  public static String[] toStrArray(String string, String delimiters) {
    Objects.requireNonNull(delimiters, "delimiters may not be null");
    if(delimiters.isEmpty()) {
      throw new IllegalArgumentException("delimiters may not be empty");
    }
    if(string == null) {
      return null;
    }
    if("".equals(string)) {
      return new String[0];
    }
    StringTokenizer tokenizer = new StringTokenizer(string, delimiters);
    String[] array = new String[tokenizer.countTokens()];
    for(int i = 0; i < array.length; i++) {
      array[i] = tokenizer.nextToken();
    }
    return array;
  }

  /**
   * Returns an array whose contents are the toString results for the elements
   * in the collection specified
   * @param collection
   * @return arrayOfString
   */
  public static String[] toStrArray(Collection<? extends Object> data) {
    String[] array = new String[data.size()];
    Iterator<? extends Object> i = data.iterator();
    int index = 0;
    while(i.hasNext()) {
      Object next = i.next();
      array[index++] = next == null ? null : next.toString();
    }
    return array;
  }

  /**
   * Returns the index of the text in the specified array or -1 if it is not found.
   * String equals() is used to compare.
   * @param value
   * @param choices
   * @return index or -1 if not present
   */
  public static int toIndex(String value, String[] choices) {
    for(int i = 0; i < choices.length; i++) {
      if(choices[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Convert a KeyedValues to a string representation
   * @param kv
   * @return string
   */
  public static String toStr(KeyedValues kv) {
    StringBuffer buffer = new StringBuffer();
    int n = kv.getItemCount();
    for(int i = 0; i < n; i++) {
      @SuppressWarnings("rawtypes")
      Comparable key = kv.getKey(i);
      Number value = kv.getValue(i);
      buffer.append(key);
      buffer.append("=");
      buffer.append(value);
      if(i < n - 1) {
        buffer.append(",");
      }
    }
    return buffer.toString();
  }

  /**
   * Convert the object to a primitive double. For Boolean this will give 1 or 0
   * for true and false. For numbers the double value is used, for null the
   * result is 0. Everything else will have its toString() parsed as a double.
   * (If such parsing fails the result will be zero).
   * @param object
   * @return double
   */
  @SuppressWarnings("rawtypes")
  public static double toDouble(Object object) {
    if(object == null) {
      return 0d;
    }
    if(object instanceof Number) {
      return ((Number) object).doubleValue();
    }
    if(object instanceof Boolean) {
      return ((Boolean) object).booleanValue() ? 1d : 0d;
    }
    if(object instanceof Map.Entry) {
      return toDouble(((Map.Entry) object).getValue());
    }
    try {
      return Double.parseDouble(object.toString());
    } catch(Exception e) {
      return 0d;
    }
  }

  /**
   * Parse string into an int. If this fails returns 0.
   * @param value
   * @return intValue
   */
  public static int toInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch(Exception e) {
      return 0;
    }
  }

  /**
   * Parse steing into a double. If this fails feturn zero.
   * @param value
   * @return doubleValue
   */
  public static long toLong(String value) {
    try {
      return Long.parseLong(value);
    } catch(Exception e) {
      return 0;
    }
  }

  /**
   * Format a double as a string with the specified number of fraction digits
   * @param dbl
   * @param digits
   */
  public static String toStr(double dbl, int digits) {
    //NumberFormat nf = NumberFormat.getNumberInstance();
    NumberFormat nf = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
    nf.setMaximumFractionDigits(digits);
    nf.setMinimumFractionDigits(digits);
    return nf.format(dbl);
  }

  /**
   * Format a Color a string of red,green,blue decimal values
   * @param color
   * @return colorString
   */
  public static String toStr(Color color) {
    if(color == null) {
      return null;
    }
    return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
  }

  /**
   * Given a string of red,green,blue decimal values will return a Color. If
   * invalid will return null.
   * @param rgb
   *          a red,green,blue string
   * @return color
   */
  public static Color toColor(String rgb) {
    try {
      StringTokenizer tokenizer = new StringTokenizer(rgb, ",");
      String redStr = tokenizer.nextToken();
      String greenStr = tokenizer.nextToken();
      String blueStr = tokenizer.nextToken();
      int red = toInt(redStr);
      int green = toInt(greenStr);
      int blue = toInt(blueStr);
      return new Color(red, green, blue);
    } catch(Exception bad) {
      return null;
    }
  }

  /**
   * Given an x,y coordinate and x and y deltas, will determine the distance
   * (rounds down to the nearest integer)
   * @param oX
   *          The x coordinate
   * @param oY
   *          Teh y coordinate
   * @param dX
   *          the x delta
   * @param dY
   *          the y delta
   * @return distance
   */
  public static int toDistance(int oX, int oY, int dx, int dy) {
    float x = dx - oX;
    float y = dy - oY;
    float result = (float) Math.sqrt((x * x) + (y * y));
    return (int) Math.floor(result);
  }

  /**
   * Substring replacement
   * @param string
   *          the source string
   * @param string
   *          to replace
   * @param string
   *          to replace it with
   * @return newString
   */
  public static String replace(String string, String oldStr, String newStr) {
    Objects.requireNonNull(string, "string may not be null");
    Objects.requireNonNull(oldStr, "oldStr may not be null");
    
    if(oldStr.isEmpty()) {
      throw new IllegalArgumentException("oldStr may not be empty");
    }
    
    if(newStr == null) {
      newStr = "";
    }
    StringBuffer buffer = new StringBuffer(string.length());
    int index = 0;
    int posOfOldStr = string.indexOf(oldStr, index);
    while(posOfOldStr != -1) {
      String withoutOldStr = string.substring(index, posOfOldStr);
      buffer.append(withoutOldStr);
      buffer.append(newStr);
      index = posOfOldStr + oldStr.length();
      posOfOldStr = string.indexOf(oldStr, index);
    }
    if(index < string.length()) {
      String remainderStr = string.substring(index);
      buffer.append(remainderStr);
    }
    String result = buffer.toString();
    return result;
  }

  public static String wordWrap(String text, int maxColumns) {
    if(text == null || text.length() <= maxColumns || text.length() == 0) { //Very simple cases that require no processing
      return text;
    }

    int position = 0;
    //Queue to hold the words of a line. (1.4 doesnt have Queue)
    ArrayList<String> paragraphWords = new ArrayList<String>(32);
    //Buffer for output
    StringBuffer buffer = new StringBuffer();
    boolean more = true;

    process_paragraph: while(more) {
      int eol = text.indexOf('\n', position);
      if(eol == -1)
        eol = text.length();
      more = (eol < text.length());
      String paragraph = text.substring(position, eol);
      position = eol + 1;

      if(paragraph.length() < maxColumns) { //Paragraphs shorter than #columns are appended as-is
                                              //(The xFactor tutorial text relies on this!)
        buffer.append(paragraph);
        buffer.append('\n');
        continue process_paragraph;
      }
      //else...
      StringTokenizer tokenizer = new StringTokenizer(paragraph);
      while(tokenizer.hasMoreElements())
        paragraphWords.add(tokenizer.nextToken());

      while(paragraphWords.size() > 0) {
        ArrayList<String> lineWords = new ArrayList<String>();
        int column = 0;
        int charsUsed = 0;
        while(column < maxColumns && paragraphWords.size() > 0) { //Populate lineWords with all the words that will fit
          String word = (String) paragraphWords.get(0);
          int wordLength = word.length();
          column += wordLength;
          if(column < maxColumns - 1 || column == wordLength) { //Add the word only if it fits (not including trailing space)
                                                                  //or if its the first word in which case it has to be included or it never will be
                                                                //as we dont implement splitting of individual words. (An example of where this situation
                                                                //occurs is in importingUnits.txt where we have a long line with no spaces as an example
                                                                //of column definitions)
            lineWords.add(word);
            paragraphWords.remove(0);
            charsUsed += word.length();

          }

          column++; //Allocate room for at least one space after the word
          charsUsed++;

        }
        int extraSpaces = maxColumns - charsUsed;
        while(lineWords.size() > 0) {
          String word = (String) lineWords.remove(0);
          buffer.append(word);
          buffer.append(' ');
          if(extraSpaces > 0) {
            buffer.append(' ');
            extraSpaces--;
          }
        }
        buffer.append('\n');
      }

    }

    return buffer.toString();
  }

  /**
   * Performs word wrapping to the specified number of columns on a large
   * string. (Current impl merely forces a break at the specified column. We
   * hope to improve this to actually wrap based on words in a future version)
   * @param string
   * @param columns
   * @return formattedText
   */
  public static final String wordWrapOLD(String string, int columns) {
    if(string == null || string.length() <= columns || string.length() == 0) {
      return string;
    }

    /*
     * int index =0; StringBuffer buffer = new StringBuffer(string.length() +
     * string.length() / columns); String line = ""; while(index <
     * string.length() + line.length()) { boolean addNl = true; int endOfLine =
     * string.indexOf("\n",index); if(endOfLine == -1) { endOfLine =
     * string.length(); addNl = false; } line =
     * string.substring(index,endOfLine);
     * 
     * if(line.length() <= columns) { buffer.append(line); index += endOfLine;
     * if(addNl) { buffer.append("\n"); index++; } } else //split line { line =
     * line.substring(0,columns); buffer.append(line); buffer.append("\n");
     * index += columns; }
     * 
     * }
     */

    StringBuffer buffer = new StringBuffer(string);
    //int initLength = buffer.length(); //this var for debugging. Dont use as it changes
    int i = 0;
    int n = 0;
    while(i < buffer.length()) {
      n = buffer.indexOf("\n", i);
      if(n == -1 || n - i >= columns) {
        n = i + columns;
        if(n < buffer.length()) {
          buffer.insert(n, "\n");
          i = n + 1;
        }
        i = n;
      } else {
        i = n + 1;
      }
    }
    String result = buffer.toString();
    return result;
  }

  /**
   * Returns the string form of the MD5 hash of the specified data. nb: don't
   * use MD5 for security stuff! The format that is output here is just the
   * toString of a BigInteger created from the digest and so uses lowercase hex
   * and may have negative values. In other words, not quite standard format and
   * really just intended for internal use in Romeo (eg: for applying Unit data
   * import adjustments)
   * @param data
   * @return md5 hash as a string
   */
  public static String toDigestSignature(byte[] data) {
    try {
      return new BigInteger(MessageDigest.getInstance("MD5").digest(data)).toString(16);
    } catch(Exception e) {
      throw new RuntimeException("Failed to get MD5 digest", e);
    }
  }

  /**
   * Will split a comma delimited string into a list of String. Follows the
   * following rules for csvs: -Fields are delimited by a comma, and may
   * optionally be surrounded by double quotes -Whitespace will be included in
   * the field values -Fields that contain a quote must be quoted and quotes in
   * a field value must be escaped with a second quote -Newlines are allowed but
   * any field with them must be quoted -Commas inside of the quotes are
   * considered as part of the field value and not a delimiter -Whitespace is
   * not trimmed Violations of these rules will result in an
   * IllegalArgumentException being raised. An empty string is considered as a
   * single field containing the empty string (so a csv containing only a comma
   * would result in two fields both of which are empty strings)
   * @param csv
   *          the csv string to parse. May be empty but not null
   * @return strings
   * @throws IllegalArgumentException
   */
  public static List<String> fromCsv(String csv) {
    Objects.requireNonNull(csv, "csv may not be null");
    if(csv.isEmpty()) {
      return Arrays.asList("");
    }
    List<String> columns = new ArrayList<String>();

    int index = 0;
    int quoteStartedAt = -1;
    boolean quoted = false;
    boolean appendExtraEmptyField = false;
    while(index < csv.length()) {
      StringBuilder value = new StringBuilder(); //for assembling this columns value    
      int length = csv.length();

      if(csv.charAt(index) == '"') {
        //Fields may be quoted, in which case first char of the field is a quote
        quoted = true;
        quoteStartedAt = index;
        index++; //we dont want the start quote in the value
      }

      boolean seeking = true;
      while(seeking) {
        char c = csv.charAt(index);
        boolean isLastChar = index + 1 >= length;
        char nextChar = isLastChar ? ',' : csv.charAt(index + 1); //nb: if end of csv, we pretend next char is a comma

        if(c == '"') {
          //We found a (non-starting) quote. 
          if(!quoted) {
            //A field that contains quotes mustbe quoted so this is illegal
            throw new IllegalArgumentException("unquoted quote at position " + index);
          }
          if(nextChar == ',') {
            //A quote while quoted followed by a comma (or end of string disguised as one_ indicates the end of the field
            seeking = false;
            index++; //We start processing next field from the char after the comma we found
            quoted = false; //exit quoted mode
            quoteStartedAt = -1;
          } else if(nextChar == '"') {
            //If next character is also a quote then this is the escaping for a quote in a quoted field
            //and we only want to copy one of them into the value, so we pre-emptively increase the index
            //here to skip the processing of the second one in the next round of the seeking loop. 
            index++;
          } else {
            //A quoted quote can either be the ending of that quote or an embedded one in which
            //case it must be escaped
            throw new IllegalArgumentException("unescaped quote at position " + index);
          }

        } else if(c == ',' && !quoted) {
          //If we are in quotes then a comma is treated as any other character but
          //if we aren't then its the end of the field
          seeking = false;
          //and if the csv line ends with a comma then a blank field after it is implied
          if(isLastChar) {
            appendExtraEmptyField = true;
          }
        } else if(c == '\n' && !quoted) {
          //All newlines must be quoted. An unquoted newline is the end of record terminator, and this function
          //expects those to have been handled before calling. ie: it doesn't support them (even at the end of the string)
          throw new IllegalArgumentException("unquoted newline at position " + index);
        }

        if(seeking) {
          value.append(c);
        }
        index++;
        if(index >= length) {
          seeking = false;
        }
      } //endwhile seeking
      columns.add(value.toString());
      if(quoted) {
        throw new IllegalArgumentException("unterminated quote at position " + quoteStartedAt);
      }
    } ///endwhile nextStart in range
    if(appendExtraEmptyField) {
      columns.add("");
    }
    return Collections.unmodifiableList(columns);
  }

  /**
   * Returns the specified list in the form of a csv string, with each entry
   * delimited by a comma. Strings that contain a comma, newline, or quote
   * character " will be enclosed in quotes and the quotes will be escaped by an
   * extra quote.
   * @param fieldValuesList
   *          must contain at least one entry (though it may be an empty string)
   * @return csv
   */
  public static String toCsv(List<String> fieldValuesList) {
    Objects.requireNonNull(fieldValuesList, "fieldValuesList may not be null");
    if(fieldValuesList.isEmpty()) {
      throw new IllegalArgumentException("fieldValuesList may not be empty");
    }
    StringBuilder csv = new StringBuilder();

    //for each string we need to check if it has any embedded quotes, commas or newlines, if so it needs to be quoted and
    //embedded quotes will need to be escaped. We may as well build the csv string in the process.We could 'cheat' and always
    //quote values but we would rather not do that

    Iterator<String> iterator = fieldValuesList.iterator();
    while(iterator.hasNext()) {
      String value = Objects.requireNonNull(iterator.next(), "fieldValuesList may not contain null elements");
      boolean quoted = false;
      if(!value.isEmpty()) {
        int length = value.length();
        StringBuilder buffer = new StringBuilder(length + 16);
        int index = 0;
        while(index < length) {
          char c = value.charAt(index);
          switch (c){
            case ',':
            case '\n':
              quoted = true;
              break;
            case '"':
              quoted = true;
              buffer.append('"');
              break;
          }
          buffer.append(c);
          index++;
        }
        if(quoted) {
          csv.append('"');
          csv.append(buffer);
          csv.append('"');
        } else {
          csv.append(buffer);
        }
      } //endif not an empty value
      if(iterator.hasNext()) {
        csv.append(',');
      }
    } //next value
    return csv.toString();
  }

  /**
   * If the source string is properly quoted, this will strip the quotes and
   * return the contents. Throws an exception if the quote isnt closed. WARNING:
   * Currently this just takes the naive approach which is to check if the first
   * in last characters are a quote, and fail if not, and strip them if so,
   * without paying any attention to whether there are other unescaped quotes
   * within the string.
   * @param source
   * @return
   */
  public static String toUnquotedString(String source) {
    Objects.requireNonNull(source, "source string may not be null");
    if(source.startsWith("\"")) { 
      if(!source.endsWith("\"")) {
        throw new IllegalArgumentException("Unterminated string value" + source);
      }
      source = source.substring(1, source.length() - 1);
    }
    return source;
  }
  
  /**
   * Returns an array of primitive ints. If the collection of numbers is null will return null.
   * Null elements in the collection are transformed into 0 values.
   * The order of elements in the array will be the encounter order of iterating the collection.
   * @param numbers
   * @return ints
   */
  public static int[] toPrimitiveIntArray(Collection<? extends Number> numbers) {
    if(numbers == null) { return null; }
    int[] ints = new int[ numbers.size() ];
    int i=0;
    for(Number number : numbers) {
      ints[i++] = (number==null) ? 0 : number.intValue();
    } 
    return ints;
  }
  
  /**
   * Remove all entries from the map whose key in not in the set of keys.
   * @param map
   * @param keys
   */
  public static <K,V> void constrainToKeys(Map<K,V> map, Set<K> keys) {
    Set<K> allKeys = new HashSet<>(map.keySet()); //need to copy as keySet is map-backed and we are modifying it
    for(K key : allKeys) {
      if(!keys.contains(key)) {
        map.remove(key);
      }
    }
  }
  
  /**
   * Will iterate the list and replace strings by their trimmed version if the trimming results in a new
   * string. Null elements are ignored. The collection must be modifiable and may not be null.
   * @param strings
   */
  public static void trimStringsInPlace(List<String> strings) {
    Objects.requireNonNull(strings, "strings may not be null");
    ListIterator<String> i = strings.listIterator();
    while(i.hasNext()) {
      String string = i.next();
      if(string != null) {
        String trimmed = string.trim();
        if(trimmed != string) {
          i.set(trimmed);
        }
      }
    }
  }
  
  /**
   * Will return a new modifiable list that contains the trimmed versions of the strings 
   * in the supplied list.
   * @param strings
   * @return trimmed strings
   */
  public static List<String> trimStrings(List<String> strings) {
    //yes, the in place version was written first
    List<String> trimmed = new ArrayList<>(strings);
    trimStringsInPlace(trimmed);
    return trimmed;
  }
  
//  public static void main(String[] a) {
//    System.out.println(Convert.toDigestSignature("TK Psi Hauler".getBytes()));
//  }
}
