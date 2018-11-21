package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.expressions.Comparison.ComparisonOperand;

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
      new Comparison(null, ComparisonOperand.EQUAL, new Value(0));
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Comparison(new Value(0), ComparisonOperand.EQUAL, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testNotEqual() {
    assertTrue( (Boolean)new Comparison(PI,ComparisonOperand.NOT_EQUAL,THREE).evaluate(_context) );    
    assertTrue( (Boolean)new Comparison(HELLO,ComparisonOperand.NOT_EQUAL,WORLD).evaluate(_context) );    
    assertFalse( (Boolean)new Comparison(PI,ComparisonOperand.NOT_EQUAL,PI).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(TRUE,ComparisonOperand.NOT_EQUAL,TRUE).evaluate(_context) );    
    assertTrue( (Boolean)new Comparison(TRUE,ComparisonOperand.NOT_EQUAL,HELLO).evaluate(_context) );
  }
  
  @Test
  public void testEqual() {
    assertTrue( (Boolean)new Comparison(PI,ComparisonOperand.EQUAL,PI).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(THREE,ComparisonOperand.EQUAL,THREE).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(TRUE,ComparisonOperand.EQUAL,TRUE).evaluate(_context) );    
    assertFalse( (Boolean)new Comparison(TRUE,ComparisonOperand.EQUAL,THREE).evaluate(_context) );
  }
  
  @Test
  public void testGreaterThan() {
    assertTrue( (Boolean)new Comparison(PI,ComparisonOperand.GREATER_THAN,THREE).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(ONE,ComparisonOperand.GREATER_THAN,PI).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(ONE,ComparisonOperand.GREATER_THAN,ONE).evaluate(_context) );
  }
  
  @Test
  public void testGreaterOrEqual() {
    assertTrue( (Boolean)new Comparison(PI,ComparisonOperand.GREATER_OR_EQUAL,THREE).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(ONE,ComparisonOperand.GREATER_OR_EQUAL,PI).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(ONE,ComparisonOperand.GREATER_OR_EQUAL,ONE).evaluate(_context) );
  }
  
  @Test
  public void testLessThan() {
    assertTrue( (Boolean)new Comparison(ONE,ComparisonOperand.LESS_THAN,THREE).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(PI,ComparisonOperand.LESS_THAN,PI).evaluate(_context) );
  }
  
  @Test
  public void testLessOrEqual() {
    assertTrue( (Boolean)new Comparison(ONE,ComparisonOperand.LESS_OR_EQUAL,THREE).evaluate(_context) );
    assertTrue( (Boolean)new Comparison(PI,ComparisonOperand.LESS_OR_EQUAL,PI).evaluate(_context) );
    assertFalse( (Boolean)new Comparison(THREE,ComparisonOperand.LESS_OR_EQUAL,ONE).evaluate(_context) );
  }
  
  
}
