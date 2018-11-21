package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.expressions.Adjust.AdjustOperand;

public class TestAdjust {

  public static final double D = 0.0000001;
  
  private RoundContext _context;
  
  @Before
  public void setup() {
    _context = new RoundContext(new String[] { "a","b" });
  }
  
  @Test
  public void testRound() {
    assertEquals( 2.0, (Double)new Adjust(new Value(1.6), AdjustOperand.ROUND).evaluate(_context), D);
    assertEquals( 2.0, (Double)new Adjust(new Value(1.5), AdjustOperand.ROUND).evaluate(_context), D);
    assertEquals( 1.0, (Double)new Adjust(new Value(1.2), AdjustOperand.ROUND).evaluate(_context), D);    
  }
  
  @Test
  public void testFloor() {
    assertEquals( 1.0, (Double)new Adjust(new Value(1.6), AdjustOperand.FLOOR).evaluate(_context), D);
    assertEquals( 1.0, (Double)new Adjust(new Value(1.5), AdjustOperand.FLOOR).evaluate(_context), D);
    assertEquals( 1.0, (Double)new Adjust(new Value(1.2), AdjustOperand.FLOOR).evaluate(_context), D); 
  }
  
  @Test
  public void testCeiling() {
    assertEquals( 2.0, (Double)new Adjust(new Value(1.6), AdjustOperand.CEILING).evaluate(_context), D);
    assertEquals( 2.0, (Double)new Adjust(new Value(1.5), AdjustOperand.CEILING).evaluate(_context), D);
    assertEquals( 2.0, (Double)new Adjust(new Value(1.2), AdjustOperand.CEILING).evaluate(_context), D); 
  }
  
  @Test
  public void testConstructor() {
    try {
      Adjust a = new Adjust( new Value(0), null );
      fail("Expected NullPointerException but found " + a);
    } catch(NullPointerException expected) {}
    
    try {
      new Adjust( null, AdjustOperand.ROUND);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testDodgyValues() {
    //expressions whose results don't convert to a Double are treated as returning 0
    assertEquals(0.0, (Double)new Adjust( new Value("hello"), AdjustOperand.FLOOR ).evaluate(_context), D);
    assertEquals(123.0, (Double)new Adjust( new Value("123.40"), AdjustOperand.FLOOR ).evaluate(_context), D);
    assertEquals(1.0, (Double)new Adjust( new Value(true), AdjustOperand.FLOOR ).evaluate(_context), D);
    assertEquals(0.0, (Double)new Adjust( new Value(false), AdjustOperand.FLOOR ).evaluate(_context), D);
  }
  
  @Test
  public void testAdjustOperandFromString() {
    
    assertEquals( AdjustOperand.ROUND, AdjustOperand.fromString("ROUND") );
    assertEquals( AdjustOperand.FLOOR, AdjustOperand.fromString("FLOOR") );
    assertEquals( AdjustOperand.CEILING, AdjustOperand.fromString("CEILING") );
    assertEquals( AdjustOperand.ROUND, AdjustOperand.fromString("rOunD") );
    assertEquals( AdjustOperand.FLOOR, AdjustOperand.fromString("Floor") );
    assertEquals( AdjustOperand.CEILING, AdjustOperand.fromString("ceiliNG") );
    
    try {
      AdjustOperand.fromString("blah");
    } catch(IllegalArgumentException expected) {}
    
    try {
      AdjustOperand.fromString(" ROUND"); //whitespace is also not handled here
    } catch(IllegalArgumentException expected) {}
    
  }
  
}
