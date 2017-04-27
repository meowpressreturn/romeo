package romeo.persistence;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestAbstractRecordId {

  /**
   * AbstractRecordId is abstract so we use this concrete class to exercise it in our tests
   */
  private static class TestId extends AbstractRecordId {
    public TestId(String id) {
      super(id);
    }    
  }
  
  /**
   * This class differs only from TestId in its name. (We use it in the equality test to verify
   * that different classes dont evaluate equal)
   */
  private static class AnotherTestId extends AbstractRecordId {
    public AnotherTestId(String id) {
      super(id);
    }    
  }
  
  /**
   * This class inherits from TestId but shouldnt evaluate as equal to id because its not the
   * same exact class.
   */
  private static class TestIdSubclass extends AbstractRecordId {
    public TestIdSubclass(String id) {
      super(id);
    }    
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  @Test
  public void testConstructor() {
    //nb: for historical reasons we aren't enforcing the format (eg 24 hex chars) as
    //    its possible there are old database out there using something else that  would
    //    then fail to import.
    AbstractRecordId id = new TestId("123Foo");
    assertEquals( "123Foo", id.toString() );
    
    try {
      new TestId(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new TestId("");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new TestId("     ");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testEqualsHashCode() {
    AbstractRecordId id1 = new TestId("abc123");
    int hashcode1 = id1.hashCode();
    
    AbstractRecordId id2 = new TestId("abc123");
    int hashcode2 = id2.hashCode();
    
    //Objects that are equal should have the same hashcode
    assertEquals(id1, id2);
    assertEquals(hashcode1, hashcode2);
    
    //We don't really need to test what hashcodes it actually returns as that isnt
    //defined by the interface beyond the above, but we intended impl to follow what the
    //string had, so we will test that here too
    assertEquals("abc123".hashCode(), id1.hashCode());
  }
  
  @Test
  public void testEqualsOtherObjects() {
    assertTrue( new TestId("12345").equals( new TestId("12345")) ); //lets be sure we are sane first
    
    assertFalse( "abcd1234".equals(new TestId("abcd1234")) );
    assertFalse( new TestId("12345").equals(new AnotherTestId("12345")) );
    assertFalse( new TestId("12345").equals(new AnotherTestId("12345")) );
    assertFalse( new TestId("12345").equals(new TestIdSubclass("12345")) );
    
    //It shouldnt barf on nulls
    assertFalse( new TestId("12345").equals(null) );
    
    //And should of course be equals to itself
    AbstractRecordId ownself = new TestId("memyselfandi");
    assertTrue( ownself.equals(ownself) );
  }
  
  
}



















