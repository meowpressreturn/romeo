package romeo.persistence;

import java.util.Objects;

/**
 * Represents the id value of a record. Intended to be subclassed with specific implementations for
 * different types of record.
 * Allows for better type checking and validation than just using a String.
 */
public abstract class AbstractRecordId {
  private String _id;
  
  public AbstractRecordId(String id) {
    _id = Objects.requireNonNull(id, "id may not be null").trim();
    if(_id.isEmpty()) {
      throw new IllegalArgumentException("id may not be empty");
    }
    //nb: for historical reasons we aren't enforcing the format (eg 24 hex chars) as
    //    its possible there are old database out there using something else that  would
    //    then fail to import.
  }
  
  @Override
  public String toString() {
    return _id;
  }
  
  @Override
  public int hashCode() {
    return _id.hashCode();
  }
  
  /**
   * Returns true only if the other object is the same class as this (not a subclass) and has the
   * same value.
   * @param o
   */
  @Override
  public boolean equals(Object o) {
    if(this==o) {
      return true;
    } else if(o==null || !getClass().equals(o.getClass())) {
      return false;
    } else {
      return _id.equals(((AbstractRecordId)o)._id);    
    }
  }
}



















