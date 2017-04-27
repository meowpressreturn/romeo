package romeo.utils;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TestAccumulator {
  
  @Test
  public void testSetValue() {
   Accumulator a = new Accumulator();
   a.setValue("hello", 123);
   assertEquals(123, a.getValue("hello"));
  }

  @Test
  public void testAddToValue() {
    Accumulator a = new Accumulator();
    a.addToValue("foo", 100);
    assertEquals(100, a.getValue("foo"));
    a.addToValue("bar", 200);
    assertEquals(100,a.getValue("foo"));
    assertEquals(200,a.getValue("bar"));
    a.addToValue("foo",500);
    assertEquals(600, a.getValue("foo"));    
    a.addToValue("foo", -1);
    assertEquals(599, a.getValue("foo"));
    a.addToValue("foo", -1000);
    assertEquals(-401, a.getValue("foo")); 
  }
  
  @Test
  public void testReturnsZeroForMissingKeys() {
    Accumulator a = new Accumulator();
    assertEquals(0, a.getValue("noSuchKey"));
  }
  
  @Test
  public void testGetKeys() {
    Accumulator a = new Accumulator();
    Object[] keys = a.getKeys();
    assertArrayEquals(new Object[] {} , keys);    
    a.setValue("foo", 1);
    a.addToValue("bar", 1);
    a.setValue("zero", 0);
    keys = a.getKeys();
    Arrays.sort(keys); //we can't make assumptions about the order of the returned array so sort explicitly!
    assertArrayEquals( new Object[] { "bar","foo","zero" }, keys);
    
    Accumulator b = new Accumulator();
    assertArrayEquals( new Object[] {} , b.getKeys() );
  }
  
  @Test
  public void testSettingToZeroDoesntRemoveKey() {
    Object key = new Integer(42);
    Accumulator a = new Accumulator();
    a.setValue(key, 12345);
    a.setValue(key, 0);
    Object[] keys = a.getKeys();
    assertArrayEquals( new Object[] { key }, keys );
  }
  
  @Test
  public void testClearRemovesAllKeys() {
    Accumulator a = new Accumulator();
    a.setValue("foo", 1);
    a.addToValue("bar", 1);
    a.setValue("zero", 0);
    assertEquals(3, a.getKeys().length);
    a.clear();
    assertEquals(0, a.getKeys().length);
    assertEquals(0, a.getValue("foo"));
    assertArrayEquals( new Object[] {} , a.getKeys());
  }
  
  @Test
  public void testNullKeyIsForbidden() {
    Accumulator a = new Accumulator();
    try {
      a.setValue(null, 123);
      fail("expected a NullPointerException");
    } catch(NullPointerException expected) {
      //ok
    }
    
    try {
      a.addToValue(null, 123);
      fail("expected a NullPointerException");
    } catch(NullPointerException expected) {
      //ok
    }
    
    try {
      int v = a.getValue(null);
      fail("expected a NullPointerException, but got " + v);
    } catch(NullPointerException expected) {
      //ok
    }
  }
  
}
