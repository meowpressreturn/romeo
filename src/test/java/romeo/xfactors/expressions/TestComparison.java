package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestComparison {
  
  private static final IExpression THREE = new Value(3);
  private static final IExpression TRUE = new Value(true);
  private static final IExpression ONE = new Value(1);
  private static final IExpression PI = new Value(Math.PI);
  private static final IExpression HELLO = new Value("hello");
  private static final IExpression WORLD = new Value("world");

  private RoundContext _context;
  
  @Before
  public void setup() {
    _context = new RoundContext(new String[] {});
  }  
  
  @Test
  public void testConstructor() {
    try {
      new Comparison(null, Comparison.EQUAL, new Value(0));
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Comparison(new Value(0), Comparison.EQUAL, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();
    
    Comparison notEqual = new Comparison("VALUE(0),NOT_EQUAL,VALUE(0)", parser);
    assertEquals( Comparison.NOT_EQUAL, notEqual.getOperand() );
    assertTrue( notEqual.getLeft() instanceof Value);
    assertTrue( notEqual.getRight() instanceof Value);
    
    assertEquals(Comparison.EQUAL, new Comparison("VALUE(0),EQUAL,VALUE(0)",parser).getOperand() );
    assertEquals(Comparison.GREATER_THAN, new Comparison("VALUE(0),GREATER_THAN,VALUE(0)",parser).getOperand() );
    assertEquals(Comparison.GREATER_OR_EQUAL, new Comparison("VALUE(0),GREATER_OR_EQUAL,VALUE(0)",parser).getOperand() );
    assertEquals(Comparison.LESS_THAN, new Comparison("VALUE(0),LESS_THAN,VALUE(0)",parser).getOperand() );
    assertEquals(Comparison.LESS_OR_EQUAL, new Comparison("VALUE(0),LESS_OR_EQUAL,VALUE(0)",parser).getOperand() );
    
    try {
      new Comparison(null, parser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Comparison("VALUE(0)",null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Comparison(",,,,,", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Comparison("VALUE(0),NOT_EQUAL,VALUE(0),", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Comparison("VALUE(0),NOT_EQUAL,VALUE(0),VALUE(0)", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testNotEqual() {
    assertTrue( (Boolean)new Comparison(PI,Comparison.NOT_EQUAL,THREE).evaluate(_context) );    
    assertTrue( (Boolean)new Comparison(HELLO,Comparison.NOT_EQUAL,WORLD).evaluate(_context) );    
    assertFalse( (Boolean)new Comparison(PI,Comparison.NOT_EQUAL,PI).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(TRUE,Comparison.NOT_EQUAL,TRUE).evaluate(_context) );    
    assertTrue( (Boolean)new Comparison(TRUE,Comparison.NOT_EQUAL,HELLO).evaluate(_context) );
  }
  
  @Test
  public void testEqual() {
    assertTrue( (Boolean)new Comparison(PI,Comparison.EQUAL,PI).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(THREE,Comparison.EQUAL,THREE).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(TRUE,Comparison.EQUAL,TRUE).evaluate(_context) );    
    assertFalse( (Boolean)new Comparison(TRUE,Comparison.EQUAL,THREE).evaluate(_context) );
  }
  
  @Test
  public void testGreaterThan() {
    assertTrue( (Boolean)new Comparison(PI,Comparison.GREATER_THAN,THREE).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(ONE,Comparison.GREATER_THAN,PI).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(ONE,Comparison.GREATER_THAN,ONE).evaluate(_context) );
  }
  
  @Test
  public void testGreaterOrEqual() {
    assertTrue( (Boolean)new Comparison(PI,Comparison.GREATER_OR_EQUAL,THREE).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(ONE,Comparison.GREATER_OR_EQUAL,PI).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(ONE,Comparison.GREATER_OR_EQUAL,ONE).evaluate(_context) );
  }
  
  @Test
  public void testLessThan() {
    assertTrue( (Boolean)new Comparison(ONE,Comparison.LESS_THAN,THREE).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(PI,Comparison.LESS_THAN,PI).evaluate(_context) );
  }
  
  @Test
  public void testLessOrEqual() {
    assertTrue( (Boolean)new Comparison(ONE,Comparison.LESS_OR_EQUAL,THREE).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(PI,Comparison.LESS_OR_EQUAL,PI).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(THREE,Comparison.LESS_OR_EQUAL,ONE).evaluate(_context) );
  }
  
  
}
