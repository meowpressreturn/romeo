package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;

public class TestAdjust {

  public static final double D = 0.0000001;
  
  private RoundContext _context;
  
  @Before
  public void setup() {
    _context = new RoundContext(new String[] { "a","b" });
  }
  
  @Test
  public void testRound() {
    assertEquals( 2.0, (Double)new Adjust(new Value(1.6), Adjust.ROUND).evaluate(_context), D);
    assertEquals( 2.0, (Double)new Adjust(new Value(1.5), Adjust.ROUND).evaluate(_context), D);
    assertEquals( 1.0, (Double)new Adjust(new Value(1.2), Adjust.ROUND).evaluate(_context), D);    
  }
  
  @Test
  public void testFloor() {
    assertEquals( 1.0, (Double)new Adjust(new Value(1.6), Adjust.FLOOR).evaluate(_context), D);
    assertEquals( 1.0, (Double)new Adjust(new Value(1.5), Adjust.FLOOR).evaluate(_context), D);
    assertEquals( 1.0, (Double)new Adjust(new Value(1.2), Adjust.FLOOR).evaluate(_context), D); 
  }
  
  @Test
  public void testCeiling() {
    assertEquals( 2.0, (Double)new Adjust(new Value(1.6), Adjust.CEILING).evaluate(_context), D);
    assertEquals( 2.0, (Double)new Adjust(new Value(1.5), Adjust.CEILING).evaluate(_context), D);
    assertEquals( 2.0, (Double)new Adjust(new Value(1.2), Adjust.CEILING).evaluate(_context), D); 
  }
  
  @Test
  public void testConstructor() {
    try {
      Adjust a = new Adjust( new Value(0), 123 );
      fail("Expected IllegalArgumentException but found " + a);
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Adjust( null, Adjust.ROUND);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testDodgyValues() {
    //expressions whose results don't convert to a Double are treated as returning 0
    assertEquals(0.0, (Double)new Adjust( new Value("hello"), Adjust.FLOOR ).evaluate(_context), D);
    assertEquals(123.0, (Double)new Adjust( new Value("123.40"), Adjust.FLOOR ).evaluate(_context), D);
    assertEquals(1.0, (Double)new Adjust( new Value(true), Adjust.FLOOR ).evaluate(_context), D);
    assertEquals(0.0, (Double)new Adjust( new Value(false), Adjust.FLOOR ).evaluate(_context), D);
  }
  
}
