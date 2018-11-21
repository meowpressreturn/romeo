package romeo.xfactors.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.xfactors.api.IExpression;
import romeo.xfactors.expressions.Adjust;
import romeo.xfactors.expressions.Adjust.AdjustOperand;
import romeo.xfactors.expressions.Arithmetic;
import romeo.xfactors.expressions.Comparison;
import romeo.xfactors.expressions.Context;
import romeo.xfactors.expressions.Fail;
import romeo.xfactors.expressions.Flag;
import romeo.xfactors.expressions.If;
import romeo.xfactors.expressions.Logic;
import romeo.xfactors.expressions.Present;
import romeo.xfactors.expressions.Quantity;
import romeo.xfactors.expressions.Rnd;
import romeo.xfactors.expressions.Value;

public class TestExpressionParserImpl {

  private ExpressionParserImpl _p;
  
  @Before
  public void setup() {
    _p = new ExpressionParserImpl();
  }
  
  /**
   * A test for top level instantiation of the supported expression types. Here we just test that it can instantiate each
   * of these types and we ignore the details of their params because that is a matter for the individual expression
   * class to manage in its constructor - although these tests may need updating if allowed grammar for these types changes)
   */
  @Test
  public void testInstantiations() {
    Object[][] types = new Object[][] {
      { "ADJUST(VALUE(0),FLOOR)", Adjust.class },
      { "ARITHMETIC(VALUE(2),ADD,VALUE(2))", Arithmetic.class },
      { "COMPARISON(VALUE(0),EQUAL,VALUE(0))", Comparison.class },
      { "CONTEXT(IS_BASE)", Context.class },
      { "IF(VALUE(true),VALUE(1),VALUE(2))", If.class },
      { "LOGIC(VALUE(true),OR,VALUE(false))", Logic.class },
      { "QUANTITY(THIS_PLAYER,XMC,0)", Quantity.class },
      { "RND(0,99)", Rnd.class },
      { "VALUE(123.0)", Value.class },      
      { "PRESENT(TKBv2)", Present.class },
      { "FLAG(THIS_PLAYER,VALUE(\"ORN\"))", Flag.class },
      { "FAIL(VALUE(\"oops!\"))", Fail.class },
    };
    
    for(int i=0; i<types.length;i++) {
      for(int lCase=0; lCase<2; lCase++) { //test both as in array and in lowercase
        String xfel = (String)types[i][0];
        if(lCase==1) { xfel = xfel.toLowerCase(); }
        Class<?> expectedClass = (Class<?>)types[i][1];
        IExpression expression = _p.getExpression(xfel);
        assertNotNull( "instantiation test " + i + " returned null", expression );
        assertTrue( 
            "instantiation test " + i + " expected " + expectedClass.getName() 
            + " but found " + expression.getClass().getName()
            + " for xfel=" + xfel
            , expectedClass.isAssignableFrom(expression.getClass()) );
      }
    }
    
    try {
      _p.getExpression("VALUE(0)extrajunk");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      _p.getExpression("NOSUCHEXPR()");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    //Null tests - the parser doesn't throw an exception for null or empty or the literal "NULL" string
    assertNull( _p.getExpression(null) );
    assertNull( _p.getExpression("") );
    assertNull( _p.getExpression("NULL") );
    assertNull( _p.getExpression("null") );
    
    //and this should hold true after trimming
    assertNull( _p.getExpression(" ") );    
    assertNull( _p.getExpression(" nUlL ") );
    assertNull( _p.getExpression(" \n\n\nNull") );
  }
  
  @Test
  public void testTokenise() {
    assertArrayEquals( new String[] { "FOO","BAR","BAZ" }, _p.tokenise("FOO,BAR,BAZ") ); 
    assertArrayEquals( new String[] { "FOO()","BAR","BAZ" }, _p.tokenise("FOO(),BAR,BAZ") ); 
    assertArrayEquals( new String[] { "FOO(ABC,DEF,GHI)","BAR","BAZ" }, _p.tokenise("FOO(ABC,DEF,GHI),BAR,BAZ") );
    assertArrayEquals( new String[] { "FOO(ABC(123,456),DEF,GHI)","BAR","BAZ" }, _p.tokenise("FOO(ABC(123,456),DEF,GHI),BAR,BAZ") );
    assertArrayEquals( new String[] { "FOO(\"sds)dsd\")","BAR","BAZ" }, _p.tokenise("FOO(\"sds)dsd\"),BAR,BAZ") );
    assertArrayEquals( new String[] { "FOO(\"sds,dsd\")","BAR","BAZ" }, _p.tokenise("FOO(\"sds,dsd\"),BAR,BAZ") );
    
    //testing for empty tokens splitting
    assertArrayEquals( new String[] { "FOO","","BAZ" }, _p.tokenise("FOO,,BAZ") );    
    assertArrayEquals( new String[] { "FOO","BAR","" }, _p.tokenise("FOO,BAR,") ); 
    assertArrayEquals( new String[] { "","" }, _p.tokenise(",") ); 
    
    assertArrayEquals( new String[] {} , _p.tokenise("") );
    
    try {
      _p.tokenise("ABC(");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {} 
    
    try {
      _p.tokenise("ABC(DEF()");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {} 
    
    try {
      _p.tokenise("ABC)");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {} 
    
    try {
      _p.tokenise("ABC(\")");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try{
      _p.tokenise(null);
      fail("expected NullPointerException");
    }catch(NullPointerException expected) {}
  }
  
  @Test
  public void testTrimToken() {
    assertEquals("foo", _p.trimToken("foo") );
    assertEquals("foo", _p.trimToken(" foo") );
    assertEquals("foo", _p.trimToken(" \nfoo\t\t   ") );
    
    //if the token is not a quoted string, all the whitepace is stripped out everywhere
    assertEquals("foobarbaz", _p.trimToken(" foo bar\nbaz ") );
    
    //whitespace in quoted sections remains (not the quotes also remain)
    assertEquals("\" foo bar baz \"", _p.trimToken("\" foo bar baz \"") );
    assertEquals("blah\" foo bar baz \"bob\" hello\"", _p.trimToken("blah\n\t   \" foo bar baz \"    bob  \" hello\"") );

  }
  
  
  @Test
  public void testParseAdjust() {
    {
      Adjust round = _p.parseAdjust("VALUE(0),ROUND");
      assertEquals( AdjustOperand.ROUND, round.getOperand());
      assertTrue( round.getValue() instanceof Value);
    }
    
    {
      Adjust floor = _p.parseAdjust("VALUE(0),FLOOR");
      assertEquals( AdjustOperand.FLOOR, floor.getOperand() );
    }
    
    {
      Adjust ceiling = _p.parseAdjust("VALUE(0),CEILING");
      assertEquals( AdjustOperand.CEILING, ceiling.getOperand() );
    }
    
    try {
      Adjust a = _p.parseAdjust("VALUE(0),BADOP");
      fail("Expected IllegalArgumentException but found " + a);
    } catch(IllegalArgumentException expected) {}
    
    try {
      Adjust a = _p.parseAdjust("");
      fail("Expected IllegalArgumentException but found " + a);
    } catch(IllegalArgumentException expected) {}
    
    try {
      Adjust a = _p.parseAdjust(",,,,,");
      fail("Expected IllegalArgumentException but found " + a);
    } catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseAdjust(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    {
    //Can it handle whitespace?
      Adjust round = _p.parseAdjust("VALUE(0), ROUND\n  ");
      assertEquals( AdjustOperand.ROUND, round.getOperand());
      assertTrue( round.getValue() instanceof Value);
    }
    
  }
}















