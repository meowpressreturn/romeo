package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.expressions.Arithmetic.ArithmeticOperand;

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
      new Arithmetic(null, ArithmeticOperand.ADD, new Value(0));
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testArithmeticOperandFromString() {
    
    assertEquals( ArithmeticOperand.ADD, ArithmeticOperand.fromString("ADD"));
    assertEquals( ArithmeticOperand.DIVIDE, ArithmeticOperand.fromString("DIVIDE"));
    assertEquals( ArithmeticOperand.MAX, ArithmeticOperand.fromString("MAX"));
    assertEquals( ArithmeticOperand.MIN, ArithmeticOperand.fromString("MIN"));
    assertEquals( ArithmeticOperand.MULTIPLY, ArithmeticOperand.fromString("MULTIPLY"));
    assertEquals( ArithmeticOperand.POWER, ArithmeticOperand.fromString("POWER"));
    assertEquals( ArithmeticOperand.ROOT, ArithmeticOperand.fromString("ROOT"));
    assertEquals( ArithmeticOperand.SUBTRACT, ArithmeticOperand.fromString("SUBTRACT"));
    
    assertEquals( ArithmeticOperand.ADD, ArithmeticOperand.fromString("aDd"));
    assertEquals( ArithmeticOperand.DIVIDE, ArithmeticOperand.fromString("diViDE"));
    assertEquals( ArithmeticOperand.MAX, ArithmeticOperand.fromString("Max"));
    assertEquals( ArithmeticOperand.MIN, ArithmeticOperand.fromString("miN"));
    assertEquals( ArithmeticOperand.MULTIPLY, ArithmeticOperand.fromString("MulTIPLY"));
    assertEquals( ArithmeticOperand.POWER, ArithmeticOperand.fromString("PoWeR"));
    assertEquals( ArithmeticOperand.ROOT, ArithmeticOperand.fromString("rOOt"));
    assertEquals( ArithmeticOperand.SUBTRACT, ArithmeticOperand.fromString("SUBtract"));
    
    try {
      ArithmeticOperand.fromString("other");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      ArithmeticOperand.fromString(" ADD");
      fail("Expected IllegalArgumentException"); //has whitespace which isnt handled here
    } catch(IllegalArgumentException expected) {}
    
    try {
      ArithmeticOperand.fromString(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  

  
  @Test
  public void testAdd() {
    assertEquals( 4, (Double)new Arithmetic(TWO,ArithmeticOperand.ADD,TWO).evaluate(_context), D);
    assertEquals( 2.5d, (Double)new Arithmetic(TWO,ArithmeticOperand.ADD,POINTFIVE).evaluate(_context), D);
  }
  
  @Test
  public void testSubtract() {
    assertEquals( 0, (Double)new Arithmetic(TWO,ArithmeticOperand.SUBTRACT,TWO).evaluate(_context), D);
    assertEquals( 1.5d, (Double)new Arithmetic(TWO,ArithmeticOperand.SUBTRACT,POINTFIVE).evaluate(_context), D);
  }
  
  @Test
  public void testMultiply() {
    assertEquals( 8, (Double)new Arithmetic(TWO,ArithmeticOperand.MULTIPLY,FOUR).evaluate(_context), D);
    assertEquals( 1, (Double)new Arithmetic(TWO,ArithmeticOperand.MULTIPLY,POINTFIVE).evaluate(_context), D);
  }
  
  @Test
  public void testDivide() {
    assertEquals( 8, (Double)new Arithmetic(FOUR, ArithmeticOperand.DIVIDE, POINTFIVE).evaluate(_context), D);
    assertEquals( 2, (Double)new Arithmetic(FOUR, ArithmeticOperand.DIVIDE, TWO).evaluate(_context), D);
  } 
  
  @Test
  public void testMin() {
    assertEquals( 2, (Double)new Arithmetic(FOUR, ArithmeticOperand.MIN, TWO).evaluate(_context), D);
  }
  
  @Test
  public void testMax() {
    assertEquals( 4, (Double)new Arithmetic(FOUR, ArithmeticOperand.MAX, TWO).evaluate(_context), D);
  }
  
  @Test
  public void testRoot() {
    assertEquals( 2, (Double)new Arithmetic(FOUR, ArithmeticOperand.ROOT, TWO).evaluate(_context), D);
    
    //Only square root is currently supported - if right is anything else it will fail
    try{
      double d = (Double)new Arithmetic(FOUR, ArithmeticOperand.ROOT, FOUR).evaluate(_context);
      fail("Expected UnsupportedOperationException but found " + d);
    } catch(UnsupportedOperationException expected) {}
  }
  
  @Test
  public void testPower() {
    assertEquals( 16, (Double)new Arithmetic(FOUR, ArithmeticOperand.POWER, TWO).evaluate(_context), D);
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
