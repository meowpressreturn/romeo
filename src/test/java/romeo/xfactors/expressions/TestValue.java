package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class TestValue {

  @Test
  public void testConstructor() {
    
    //Integer values
    assertEquals( new Integer(0), new Value(0).getValue() );
    assertEquals( new Long(1), new Value(1l).getValue() );
    assertEquals( new Integer(-555), new Value(-555).getValue() );
    assertEquals( new Integer(-1234), new Value(-1234).getValue() );
    
    //Double values
    assertEquals( new Double(0), new Value(0d).getValue() );
    assertEquals( new Double(0), new Value(0f).getValue() );
    
    //String values
    assertEquals( "hello world", new Value("hello world").getValue() );
    
    //bools
    assertEquals( true, new Value(true).getValue() );
    assertEquals( false, new Value(false).getValue() );
    
    //Nulls are allowed for Value
    assertNull( new Value(null).getValue() );   
    
    //Test we get an exception for unsupported classes
    try {
      new Value( new Date() );
      fail("Expected an IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Value( new StringBuffer("Hello") );
      fail("Expected an IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try { //currently floats aren't supported (nb: passing a primitive one it will implictly convert double)
      new Value( new Float(1.2f) );
      fail("Expected an IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
  }
  
}



















