package romeo.utils;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Comparator that compares two objects based on a presepecified property. It
 * can read the property value from a Bean, or if the object is a Map, will use
 * the property name as a key to retrieve the entry's value.
 */
public class BeanComparator implements Comparator<Object> {
  
  /**
   * Compare the passed values applying logic based on the object types. This is a static
   * method and so ignores instance instance options like the descending and caseSensitive flag.
   * nb: value1 and value2 should be the same type or the result is undefined.
   * @param value1
   * @param value2
   * @return result negative if value1 < value2, 0 if equal, and positive if value2 comes first
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static int compareValues(Object value1, Object value2) {
    try {
      if((value1 == null) && (value2 == null)) {
        return 0;
      } else if(value1 == null) {
        return -1;
      } else if(value2 == null) {
        return 1;
      } else if(value1 == value2) {
        return 0;
      }

      //At this point we know they arent same object and that neither is null

      //If they are Number we can compare the doubles
      //note that this doesnt actually work well for checking if two doubles are the same
      //because floating point is somewhat inacurate, but its good enough for Romeo's sorting purposes for now
      if(value1 instanceof Number && value2 instanceof Number) {
        double d1 = ((Number) value1).doubleValue();
        double d2 = ((Number) value2).doubleValue();
        if(d1 == d2) {
          return 0;
        } else {
          return d1 > d2 ? 1 : -1;
        }
      }
      
      //TODO - for ints do a long comparison, and for reals use a delta when comparing due to float innacuracy

      if(value1 instanceof Boolean && value2 instanceof Boolean) {
        boolean b1 = ((Boolean) value1).booleanValue();
        boolean b2 = ((Boolean) value2).booleanValue();
        if(b1 == b2) {
          return 0;
        } else {
          return b1 ? 1 : -1; //true is greater than false for our purposes
        }
      }

      //If they are the same type of Comparable then we can probably use compareTo to do the comparison
      //but if they are different types we don't know - eg: trying to compare an Integer in String's compareTo
      //results in a ClassCastException.
      if( (value1 instanceof Comparable && value2 instanceof Comparable) 
          && value1.getClass().equals(value2.getClass())) {
        return ((Comparable) value1).compareTo(value2);
      }

      //Failing that we just compare their toString results
      value1 = value1.toString();
      value2 = value2.toString();
      
      return ((String) value1).compareTo((String) value2);
    } catch(Exception e) {
      throw new RuntimeException("Error comparing values " + value1 + " and " + value2, e);
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  private String _property;
  private boolean _descending = false;
  private boolean _caseSensitive = true;

  /**
   * Constructor
   * @param property
   *          name of the bean property to compare on
   */
  public BeanComparator(String property) {
    _property = property;
  }

  /**
   * Constructor
   * @param property
   *          name of the bean property to compare on
   * @param descending
   *          if true inverts the sort order
   * @param caseSensitive
   *          if false, then values which are of String type will be compared in
   *          uppercase (this only applies to values that are actually strings
   *          when passed to the comparator and is not applied to the toString()
   *          results of converting other values to String for comparison)
   */
  public BeanComparator(String property, boolean descending, boolean caseSensitive) {
    _property = property;
    _descending = descending;
    _caseSensitive = caseSensitive;
  }

  /**
   * Compare the values based on the presepecified property retrieved from each
   * bean/map
   * @param bean1
   * @param bean2
   * @return result
   */
  @Override
  public int compare(Object bean1, Object bean2) {
    try {
      Object value1 = getPropertyFrom(bean1);
      Object value2 = getPropertyFrom(bean2);
      if( !_caseSensitive && value1 instanceof String) {
        value1 = ((String)value1).toUpperCase(Locale.US);
      }
      if( !_caseSensitive && value2 instanceof String) {
        value2 = ((String)value2).toUpperCase(Locale.US);
      }
      int result = compareValues(value1, value2);
      return _descending ? (result * -1) : result;
    } catch(Exception e) {
      return 0;
    }
  }

  private Object getPropertyFrom(Object bean) throws Exception {
    if(bean == null) {
      return null;
    }
    if(bean instanceof Map) {
      return ((Map<?, ?>) bean).get(_property);
    } else {
      return PropertyUtils.getProperty(bean, _property);
    }
  }

}
