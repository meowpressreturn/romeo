package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.ExpressionFailure;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestFail {

  @Test
  public void testParsingInstantiation() {
    IExpressionParser parser = new ExpressionParserImpl();
    Fail fail = new Fail("VALUE(0)",parser);
    assertTrue( fail.getExpression() instanceof Value );
    
    try {
      new Fail(null, parser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) { }
    
    try {
      new Fail("VALUE(123)",null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) { }
    
    try {
      new Fail(",,,,", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) { }
    
    try {
      new Fail("VALUE(0),VALUE(0)", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) { }
    
    try {
      new Fail("", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) { }
  }
  
  @Test
  public void testConstructor() {
    try {
      Fail fail = new Fail(null);
      fail("Expected NullPointerException but found " + fail);
    } catch(NullPointerException expected) {}
  }
  
  @Test 
  public void testFails() {
    Fail fail = new Fail(new Value("Hello World"));
    try {
      fail.evaluate( new RoundContext(new String[] {"a","b"} ) );
    } catch(ExpressionFailure expected) {
      assertEquals( "Hello World", expected.getMessage() );
    }
  }
  
}
