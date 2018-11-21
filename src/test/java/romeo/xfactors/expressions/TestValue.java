package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;
import romeo.xfactors.impl.ExpressionParserImpl;

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
  
  @Test
  public void testParsingConstructorAndEvaluate() {
    IExpressionParser parser = new ExpressionParserImpl();
    IExpressionTokeniser tokeniser = new ExpressionParserImpl();
    
    RoundContext context = new RoundContext(new String[]{});
    
    Object[][] tests = new Object[][] {
      { "1", new Long(1)  }, //[0]
      { "0", new Long(0)  },
      { "1.0", new Double(1) },
      { "\"hello\"", "hello" },
      { "   \"foo\"  ", "foo" }, //[4]
      
      { "\"   foo   \"", "   foo   " },
      { "-1234", new Long(-1234) },
      { "null", null },
      { "NULL", null },
      { "\"NULL\"", "NULL" }, //[9]
      
      { "true", true },
      { "false", false },
      { "trUE", true },
      { "fALSe", false }, //[13]      
      { "\"1,2,3\"", "1,2,3" }, //[14]
      { "\"(FOO)\"", "(FOO)" },
      
      { "\"\"", "" },
      
    };
    
    for(int t=0; t<tests.length;t++) {
      String params = (String)tests[t][0];
      Object expected = tests[t][1];
      try {
        Value v = new Value(params, parser, tokeniser);
        assertEquals( "test[" + t + "] value", expected, v.getValue() );
        assertEquals( "test[" + t + "] evaluate", expected, v.evaluate(context) );  
      } catch(Exception e) {
        throw new RuntimeException("tests[" + t + "] caused an exception!",e);
      }
    }
    
    try {
      new Value(null, parser, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Value("0", null, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Value("0", parser, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Value("", parser, tokeniser); //this is illegal - for an empty string would need to be quoted
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Value(",", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Value(",,,,,,,", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Value("1,2,3", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Value("foo", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
  }
  
}



















