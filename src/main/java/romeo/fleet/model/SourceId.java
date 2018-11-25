package romeo.fleet.model;

import java.util.Objects;

/**
 * Encapsulates an int value that is used to differentiate between source fleets within an overall fleet. 
 * A special case is specifying that the source could be any fleet. 
 * Instances are immutable, and the constructors are hidden, instead use the static factory methods to obtain an
 * instance.
 */
public class SourceId implements Comparable<SourceId> {
  
  public static class InvalidSourceIdException extends RuntimeException {
    
    public InvalidSourceIdException(String sourceId, Throwable cause) {
      super(sourceId, cause);
    }
    
  }
  
  /**
   * String name for the source ID representing any source. You may pass this to fromString(), or use it in an
   * XFEL expression. (Note that it should be compared in a case-insensitive manner).
   */
  public static String ANY = "ANY";
  
  /**
   * Prior to Romeo 0.6.4, nulls were used to represent the 'any' source. This is still supported in XFEL
   * and fromString, but is legacy and should not be used for new expressions.
   */
  @Deprecated
  public static String NULL = "NULL";
  
  private static final SourceId FOR_ZERO = new SourceId(0); //flyweight instance (minor optimisation)
  private static final SourceId FOR_ANY = new SourceId(); //flyweight instance (minor optimisation)
  
  public static SourceId forAnySource() {
    return FOR_ANY;
  }
  
  /**
   * We number the base fleet (for a defender) as 0, and this is also the default (eg: for an attacker)
   * so this method will just return the SourceId for subfleet 0.
   * @return sourceId for subfleet 0
   */
  public static SourceId forBaseOrDefault() {
    return FOR_ZERO;
  }
  
  public static SourceId fromInt(int source) {
    return (source==0) ? FOR_ZERO : new SourceId(source);
  }

  /**
   * Parses a SourceId from the string representation of an Integer or the
   * (case-insensitive) word "ANY". Prior to Romeo 0.6.4, a null was used to
   * indicate the 'any' source in XFEL and so this method also continues to
   * support the (case-insensitive) string "NULL" as an alternative to "ANY"
   * here, however such use is deprecated. nb: this method does not trim the
   * string, caller should do that if necessary
   * @param source
   *          a source id int or "any" as a string. Null reference is not
   *          allowed (though the literal string "null" is)
   * @return a source id as a SourceId
   */
  public static SourceId fromString(String source) {
    source = Objects.requireNonNull(source, "source may not be null");
    if("any".equalsIgnoreCase(source) || "null".equalsIgnoreCase(source)) { //nb: "null" is now 'legacy' support here
      return forAnySource();
    } else {
      try {
        int sourceInt = Integer.parseInt(source);
        return fromInt( sourceInt );
      } catch(NumberFormatException badString) {
        throw new InvalidSourceIdException(source,badString);
      }
    }    
  }
  
  ////////////////////////////////////////////////////////////////////////////

  private final Integer _source;
  
  private SourceId() {
    _source = null;
  }
  
  private SourceId(int source) {
    if(source < 0) {
      throw new IllegalArgumentException("source may not be negative");
    }
    _source = source;
  }
  
  /**
   * Returns the source id number, or for the special case of referring to any source, will return null
   * @return sourceId
   */
  public Integer asInteger() {
    return _source;
  }
  
  /**
   * Returns true if this source id refers to 'any' source subfleet
   * @return true if this sourceId refers to any source
   */
  public boolean isAny() {
    return _source == null;
  }
  
  public boolean isBaseOrDefault() {
    return (_source != null) && _source == 0;
  }
  
  @Override
  public int hashCode() {
    return (_source == null) ? -1 : _source.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj==null | !(obj instanceof SourceId)) {
      return false;
    } else {
      if( isAny() ) {
        return ((SourceId)obj).isAny();
      } else {      
        return _source.equals( ((SourceId)obj)._source );
      }
    }
  }

  @Override
  public int compareTo(SourceId o) {
    Objects.requireNonNull(o, "o may not be null");
    int tSource = isAny() ? -1 : _source;
    int oSource = o.isAny() ? -1 : o.asInteger();
    return tSource - oSource;
  }
  
  @Override
  public String toString() {
    return isAny() ? "ANY" : _source.toString();
  }
  
}



















