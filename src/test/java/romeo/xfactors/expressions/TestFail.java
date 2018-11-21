package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.ExpressionFailure;

public class TestFail {

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
