package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;

public class TestIf {

  public static final Value FOO = new Value("foo");
  public static final Value BAR = new Value("bar");

  private RoundContext _context;

  @Before
  public void setup() {
    _context = new RoundContext(new String[] {});
  }

  @Test
  public void testConstructor() {

    Value condition = new Value(true);
    If i = new If(condition, FOO, BAR);
    assertEquals(condition, i.getCondition());

    try {
      new If(null, FOO, BAR);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {    }

    try {
      new If(condition, null, BAR);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {    }

    try {
      new If(condition, FOO, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {    }
  }

  @Test
  public void testEvaluations() {
    assertEquals("foo", (String)new If(new Value(true),FOO,BAR).evaluate(_context));
    assertEquals("bar", (String)new If(new Value(false),FOO,BAR).evaluate(_context));    
    
    //0 and 1 get converted to bools
    assertEquals("foo", (String)new If(new Value(1),FOO,BAR).evaluate(_context));
    assertEquals("bar", (String)new If(new Value(0),FOO,BAR).evaluate(_context));
    
    //the string true (case-insensitive) is converted to boolean true
    assertEquals("foo", (String)new If(new Value("True"),FOO,BAR).evaluate(_context));
    //all other strings are considered false
    assertEquals("bar", (String)new If(new Value("faLSE"),FOO,BAR).evaluate(_context)); 
    assertEquals("bar", (String)new If(new Value("blah"),FOO,BAR).evaluate(_context));
    assertEquals("bar", (String)new If(new Value(""),FOO,BAR).evaluate(_context));    
    
    //null is also false
    assertEquals("bar", (String)new If(new Value(null),FOO,BAR).evaluate(_context));    
  }
}
