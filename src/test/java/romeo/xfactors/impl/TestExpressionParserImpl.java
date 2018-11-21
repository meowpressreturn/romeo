package romeo.xfactors.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.xfactors.api.IExpression;
import romeo.xfactors.expressions.Adjust;
import romeo.xfactors.expressions.Adjust.AdjustOperand;
import romeo.xfactors.expressions.Arithmetic;
import romeo.xfactors.expressions.Arithmetic.ArithmeticOperand;
import romeo.xfactors.expressions.Comparison;
import romeo.xfactors.expressions.Comparison.ComparisonOperand;
import romeo.xfactors.expressions.Context;
import romeo.xfactors.expressions.Context.ContextOperand;
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
  
  @Test
  public void testParseArithmetic() {

    Arithmetic add = _p.parseArithmetic("VALUE(0),ADD,VALUE(0)");
    assertEquals( ArithmeticOperand.ADD, add.getOperand() );
    assertTrue( add.getLeft() instanceof Value );
    assertTrue( add.getRight() instanceof Value);
    
    assertEquals( ArithmeticOperand.SUBTRACT, _p.parseArithmetic("VALUE(0),SUBTRACT,VALUE(0)").getOperand() );
    assertEquals( ArithmeticOperand.MULTIPLY, _p.parseArithmetic("VALUE(0),MULTIPLY,VALUE(0)").getOperand() );
    assertEquals( ArithmeticOperand.DIVIDE, _p.parseArithmetic("VALUE(0),DIVIDE,VALUE(0)").getOperand() );
    assertEquals( ArithmeticOperand.MIN, _p.parseArithmetic("VALUE(0),MIN,VALUE(0)").getOperand() );
    assertEquals( ArithmeticOperand.MAX, _p.parseArithmetic("VALUE(0),MAX,VALUE(0)").getOperand() );
    assertEquals( ArithmeticOperand.ROOT, _p.parseArithmetic("VALUE(0),ROOT,VALUE(0)").getOperand() );
    assertEquals( ArithmeticOperand.POWER, _p.parseArithmetic("VALUE(0),POWER,VALUE(0)").getOperand() );
    
    try{
      _p.parseArithmetic("VALUE(0),NOSUCHOP,VALUE(0)");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try{
      _p.parseArithmetic("");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try{
      _p.parseArithmetic(",,,");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseArithmetic(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testParseComparison() {
    
    Comparison notEqual = _p.parseComparison("VALUE(0),NOT_EQUAL,VALUE(0)");
    assertEquals( ComparisonOperand.NOT_EQUAL, notEqual.getOperand() );
    assertTrue( notEqual.getLeft() instanceof Value);
    assertTrue( notEqual.getRight() instanceof Value);
    
    assertEquals(ComparisonOperand.EQUAL, _p.parseComparison("VALUE(0),EQUAL,VALUE(0)").getOperand() );
    assertEquals(ComparisonOperand.GREATER_THAN, _p.parseComparison("VALUE(0),GREATER_THAN,VALUE(0)").getOperand() );
    assertEquals(ComparisonOperand.GREATER_OR_EQUAL, _p.parseComparison("VALUE(0),GREATER_OR_EQUAL,VALUE(0)").getOperand() );
    assertEquals(ComparisonOperand.LESS_THAN, _p.parseComparison("VALUE(0),LESS_THAN,VALUE(0)").getOperand() );
    assertEquals(ComparisonOperand.LESS_OR_EQUAL, _p.parseComparison("VALUE(0),LESS_OR_EQUAL,VALUE(0)").getOperand() );
    
    try {
      _p.parseComparison(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      _p.parseComparison(",,,,,");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseComparison("VALUE(0),NOT_EQUAL,VALUE(0),");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseComparison("VALUE(0),NOT_EQUAL,VALUE(0),VALUE(0)");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testParseContext() {
    assertEquals( ContextOperand.IS_DEFENDER, _p.parseContext("IS_DEFENDER").getOperand() );
    assertEquals( ContextOperand.IS_ATTACKER, _p.parseContext("IS_ATTACKER").getOperand() );
    assertEquals( ContextOperand.SOURCE, _p.parseContext("SOURCE").getOperand() );
    assertEquals( ContextOperand.ATTACKS, _p.parseContext("ATTACKS").getOperand() );
    assertEquals( ContextOperand.OFFENSE, _p.parseContext("OFFENSE").getOperand() );
    assertEquals( ContextOperand.DEFENSE, _p.parseContext("DEFENSE").getOperand() );
    assertEquals( ContextOperand.IS_BASE, _p.parseContext("IS_BASE").getOperand() );
    assertEquals( ContextOperand.IS_NOT_BASE, _p.parseContext("IS_NOT_BASE").getOperand() );
    assertEquals( ContextOperand.PD, _p.parseContext("PD").getOperand() );    
    
    try {
      _p.parseContext("NOSUCHOP");
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseContext("");
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseContext(",,,,");
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseContext("SOURCE,OFFENSE");
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      _p.parseContext(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
}















