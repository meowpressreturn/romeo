package romeo.fleet.model;

import org.junit.Test;

import romeo.fleet.model.SourceId.InvalidSourceIdException;

import static org.junit.Assert.*;

import java.util.Arrays;


public class TestSourceId {

  @Test
  public void testFromString() {
    
    try {
      SourceId.fromString(null);
      fail("Expected NPE");
    } catch(NullPointerException expected) {}

    SourceId s123 = SourceId.fromString("123");
    assertEquals( 123, s123.asInteger().intValue() );
    
    try {
      SourceId.fromString("-2"); //Negatives aren't allowed
    } catch(IllegalArgumentException expected) {}
    
    for(String test : Arrays.asList(SourceId.ANY,"ANY","any","anY","Any","NULL","null","Null","nUlL","NulL")) {
      SourceId any = SourceId.fromString(test);
      assertTrue(any.isAny());
      assertNull(any.asInteger());
    }
    
    for(String test : Arrays.asList("","foo","abc123","123abc","123 456", "1.5", "10E7")) {
      try {
        SourceId.fromString(test);
        fail("Expected an InvalidSourceIdException");
      } catch(InvalidSourceIdException expected) {}
    }
    
    //Untrimmed input should also fail!
    for(String test : Arrays.asList(" any","null "," 123","1 ","9\t")) {
      try {
        SourceId.fromString(test);
        fail("Expected an InvalidSourceIdException");
      } catch(InvalidSourceIdException expected) {}
    }
    
  }
  
  @Test
  public void testLegacyNullStringSupport() {
    //For legacy reasons we still support the string "null" (case-insensitive) as an alternative to "any"
    for(String test : Arrays.asList("NULL","null","Null","nUlL")) {
      SourceId any = SourceId.fromString(test);
      assertTrue(any.isAny());
      assertNull(any.asInteger());
    }
    
  }
  
  @Test
  public void testEqualsAndHashCode() {
    
    SourceId a1 = SourceId.fromInt(1);
    SourceId b1 = SourceId.fromInt(1);
    SourceId c2 = SourceId.fromInt(2);
    
    assertTrue( a1.equals(b1) );
    assertTrue( b1.equals(a1) );
    assertTrue( a1.hashCode() == b1.hashCode() ); //hashcodes are required to be equal here
    
    assertFalse( a1.equals(c2) );
    assertFalse( c2.equals(a1) );
    //nb: hashcodes are allowed to be equal here (ie: not required to be different)
    
    assertFalse( a1.equals(null) );
    
    //The special any case
    SourceId any = SourceId.forAnySource();
    SourceId any2 = SourceId.fromString("any"); //because we intern it will probably get same instance here anyway
    assertFalse( any.equals(null) );
    assertTrue( any.equals(any2) );
    assertTrue( any2.equals(any) );
    
    //any to not any
    assertFalse( any.equals(a1) );
    assertFalse( a1.equals(any) );
    
  }
  
  @Test
  public void testCompareTo() {
    
    SourceId s1 = SourceId.fromInt(1);
    SourceId s2 = SourceId.fromInt(2);
    SourceId s3 = SourceId.fromInt(3);
    SourceId s3b = SourceId.fromInt(3);
    
    assertTrue( s1.compareTo(s2) < 0 );
    assertTrue( s1.compareTo(s3) < 0 );    
    assertTrue( s2.compareTo(s3) < 0 );
    
    assertTrue( s2.compareTo(s1) > 0 );
    assertTrue( s3.compareTo(s1) > 0 );    
    
    assertTrue( s3.compareTo(s3b) == 0 );
    assertTrue( s3b.compareTo(s3) == 0 );
    
    //Self compare
    assertTrue( s1.compareTo(s1) == 0 );
    assertTrue( s2.compareTo(s2) == 0 );
    assertTrue( s3.compareTo(s3) == 0 );
    
    //Check how any compares
    SourceId any = SourceId.forAnySource();
    SourceId any2 = SourceId.fromString(SourceId.ANY);
    assertTrue( s1.compareTo(any) > 0 );
    assertTrue( s2.compareTo(any) > 0 );
    assertTrue( s3.compareTo(any) > 0 );
    assertTrue( any.compareTo(s1) < 0 );
    assertTrue( any.compareTo(s2) < 0 );
    assertTrue( any.compareTo(s3) < 0 );
    assertTrue( any.compareTo(any2) == 0 );
    assertTrue( any2.compareTo(any) == 0 );
    assertTrue( any.compareTo(any) == 0 );
  }
  
}



















