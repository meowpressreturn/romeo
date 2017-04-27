package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestArithmetic {

  private static final double D = 0.0000001;
  private static final IExpression TWO = new Value(2.0);
  private static final IExpression FOUR = new Value(4.0);
  private static final IExpression POINTFIVE = new Value(0.5);
  
  private RoundContext _context;
  
  @Before
  public void setup() {
    _context = new RoundContext(new String[] {});
  }
  
  @Test
  public void testConstructor() {
    try {
      new Arithmetic(null, Arithmetic.ADD, new Value(0));
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();
    
    Arithmetic add = new Arithmetic("VALUE(0),ADD,VALUE(0)", parser);
    assertEquals( Arithmetic.ADD, add.getOperand() );
    assertTrue( add.getLeft() instanceof Value );
    assertTrue( add.getRight() instanceof Value);
    
    assertEquals( Arithmetic.SUBTRACT, new Arithmetic("VALUE(0),SUBTRACT,VALUE(0)", parser).getOperand() );
    assertEquals( Arithmetic.MULTIPLY, new Arithmetic("VALUE(0),MULTIPLY,VALUE(0)", parser).getOperand() );
    assertEquals( Arithmetic.DIVIDE, new Arithmetic("VALUE(0),DIVIDE,VALUE(0)", parser).getOperand() );
    assertEquals( Arithmetic.MIN, new Arithmetic("VALUE(0),MIN,VALUE(0)", parser).getOperand() );
    assertEquals( Arithmetic.MAX, new Arithmetic("VALUE(0),MAX,VALUE(0)", parser).getOperand() );
    assertEquals( Arithmetic.ROOT, new Arithmetic("VALUE(0),ROOT,VALUE(0)", parser).getOperand() );
    assertEquals( Arithmetic.POWER, new Arithmetic("VALUE(0),POWER,VALUE(0)", parser).getOperand() );
    
    try{
      new Arithmetic("VALUE(0),NOSUCHOP,VALUE(0)", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try{
      new Arithmetic("", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try{
      new Arithmetic(",,,", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Arithmetic(null, parser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Arithmetic("VALUE(0)",null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testAdd() {
    assertEquals( 4, (Double)new Arithmetic(TWO,Arithmetic.ADD,TWO).evaluate(_context), D);
    assertEquals( 2.5d, (Double)new Arithmetic(TWO,Arithmetic.ADD,POINTFIVE).evaluate(_context), D);
  }
  
  @Test
  public void testSubtract() {
    assertEquals( 0, (Double)new Arithmetic(TWO,Arithmetic.SUBTRACT,TWO).evaluate(_context), D);
    assertEquals( 1.5d, (Double)new Arithmetic(TWO,Arithmetic.SUBTRACT,POINTFIVE).evaluate(_context), D);
  }
  
  @Test
  public void testMultiply() {
    assertEquals( 8, (Double)new Arithmetic(TWO,Arithmetic.MULTIPLY,FOUR).evaluate(_context), D);
    assertEquals( 1, (Double)new Arithmetic(TWO,Arithmetic.MULTIPLY,POINTFIVE).evaluate(_context), D);
  }
  
  @Test
  public void testDivide() {
    assertEquals( 8, (Double)new Arithmetic(FOUR, Arithmetic.DIVIDE, POINTFIVE).evaluate(_context), D);
    assertEquals( 2, (Double)new Arithmetic(FOUR, Arithmetic.DIVIDE, TWO).evaluate(_context), D);
  } 
  
  @Test
  public void testMin() {
    assertEquals( 2, (Double)new Arithmetic(FOUR, Arithmetic.MIN, TWO).evaluate(_context), D);
  }
  
  @Test
  public void testMax() {
    assertEquals( 4, (Double)new Arithmetic(FOUR, Arithmetic.MAX, TWO).evaluate(_context), D);
  }
  
  @Test
  public void testRoot() {
    assertEquals( 2, (Double)new Arithmetic(FOUR, Arithmetic.ROOT, TWO).evaluate(_context), D);
    
    //Only square root is currently supported - if right is anything else it will fail
    try{
      double d = (Double)new Arithmetic(FOUR, Arithmetic.ROOT, FOUR).evaluate(_context);
      fail("Expected UnsupportedOperationException but found " + d);
    } catch(UnsupportedOperationException expected) {}
  }
  
  @Test
  public void testPower() {
    assertEquals( 16, (Double)new Arithmetic(FOUR, Arithmetic.POWER, TWO).evaluate(_context), D);
  }
  
  @Test
  public void testEvalDouble() {
 
    try {
      Arithmetic.evalDouble(null, _context);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      Arithmetic.evalDouble(new Value(42d), null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    //evalDouble uses Convert.toDouble() internally
    
    //null
    assertEquals( 0d, Arithmetic.evalDouble(new Value(null), _context), D );
    //numbers
    assertEquals( 42d, Arithmetic.evalDouble(new Value(42d), _context), D );
    assertEquals( 42d, Arithmetic.evalDouble(new Value(42f), _context), D );
    assertEquals( 42d, Arithmetic.evalDouble(new Value(42), _context), D );
    assertEquals( 42d, Arithmetic.evalDouble(new Value(42l), _context), D );
    //bools
    assertEquals( 1d, Arithmetic.evalDouble(new Value(true), _context), D );
    assertEquals( 0d, Arithmetic.evalDouble(new Value(false), _context), D );
    //everything else is based on the toString
    assertEquals( 123.45d, Arithmetic.evalDouble(new Value("123.45"), _context) , D );
    assertEquals( -123456d, Arithmetic.evalDouble(new Value(-123456), _context) , D );
    //those that don't parse will return 0 rather than an exception
    assertEquals( 0d, Arithmetic.evalDouble(new Value("hello"), _context) , D );
    assertEquals( 0, Arithmetic.evalDouble(new Value(""), _context) , D );
  }
  
}
