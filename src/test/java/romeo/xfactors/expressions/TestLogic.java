package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestLogic {

  private RoundContext _context;
  
  @Before
  public void setup() {
    _context = new RoundContext(new String[] {});
  }
  
  @Test
  public void testConstructor() {
    Value left = new Value(true);
    Value right = new Value(false);
    Logic logic = new Logic( left, Logic.AND, right );
    assertEquals( left, logic.getLeft() );
    assertEquals( Logic.AND, logic.getOperand() );
    assertEquals( right, logic.getRight() );
    
    try {
      new Logic(null, Logic.AND, right);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Logic(left, Logic.AND, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Logic(left, 888, right);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();
    IExpressionTokeniser tokeniser = new ExpressionParserImpl();
    
    Logic logic = new Logic("VALUE(\"left\"),AND, VALUE(\"right\")", parser, tokeniser);
    assertTrue( logic.getLeft() instanceof Value);
    assertEquals( "left", ((Value)logic.getLeft()).evaluate(_context) );
    assertTrue( logic.getRight() instanceof Value);
    assertEquals( "right", ((Value)logic.getRight()).evaluate(_context) );
    assertEquals(Logic.AND, logic.getOperand());
    
    assertEquals(Logic.OR, new Logic("VALUE(0),OR,VALUE(0)",parser, tokeniser).getOperand() );
    assertEquals(Logic.XOR, new Logic("VALUE(0),XOR,VALUE(0)",parser, tokeniser).getOperand() );
    assertEquals(Logic.NOR, new Logic("VALUE(0),NOR,VALUE(0)",parser, tokeniser).getOperand() );
    assertEquals(Logic.NOT, new Logic("VALUE(0),NOT,VALUE(0)",parser, tokeniser).getOperand() );
    assertEquals(Logic.EQUAL, new Logic("VALUE(0),EQUAL,VALUE(0)",parser, tokeniser).getOperand() );    
    
    try {
      new Logic("",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Logic(",,",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Logic(",,,,,,,,",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Logic("VALUE(0),OR,VALUE(0),VALUE(1)",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    
    try {
      new Logic(null, parser, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Logic("VALUE(true),VALUE.AND,VALUE(true)",null, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Logic("VALUE(true),VALUE.AND,VALUE(true)",parser, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testAnd() {
    assertTrue( (Boolean)new Logic(new Value(true),Logic.AND,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),Logic.AND,new Value(false)).evaluate(_context) );   
    assertFalse( (Boolean)new Logic(new Value(true),Logic.AND,new Value(false)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(false),Logic.AND,new Value(true)).evaluate(_context) );
  }
  
  @Test
  public void testOr() {
    assertTrue( (Boolean)new Logic(new Value(true),Logic.OR,new Value(true)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(true),Logic.OR,new Value(false)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(false),Logic.OR,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),Logic.OR,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testXor() {
    assertFalse( (Boolean)new Logic(new Value(true),Logic.XOR,new Value(true)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(true),Logic.XOR,new Value(false)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(false),Logic.XOR,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),Logic.XOR,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testNor() {
    assertFalse( (Boolean)new Logic(new Value(true),Logic.NOR,new Value(true)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(true),Logic.NOR,new Value(false)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(false),Logic.NOR,new Value(true)).evaluate(_context) );    
    assertTrue( (Boolean)new Logic(new Value(false),Logic.NOR,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testNot() {
    //nb: like the other operands, this one is working on boolean left and right values
    assertFalse( (Boolean)new Logic(new Value(true),Logic.NOT,new Value(true)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(true),Logic.NOT,new Value(false)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(false),Logic.NOT,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),Logic.NOT,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testEqual() {
    //nb: like the other operands, this one is working on boolean left and right values
    assertTrue( (Boolean)new Logic(new Value(true),Logic.EQUAL,new Value(true)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(true),Logic.EQUAL,new Value(false)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(false),Logic.EQUAL,new Value(true)).evaluate(_context) );    
    assertTrue( (Boolean)new Logic(new Value(false),Logic.EQUAL,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testEvalBool() {
    
    assertTrue( Logic.evalBool( new Value(true), _context) );
    assertFalse( Logic.evalBool( new Value(false), _context) );
    
    //(Null is considered false, as is 0, a string
    // must be "TRUE" to be true or else it too is false).
    
    assertTrue(  Logic.evalBool( new Value("true"), _context) );
    assertTrue(  Logic.evalBool( new Value("tRUe"), _context) );
    assertFalse(  Logic.evalBool( new Value("false"), _context) );
    assertFalse(  Logic.evalBool( new Value("anything that isnt true"), _context) );
    
    assertTrue(  Logic.evalBool( new Value(1), _context) );
    assertTrue(  Logic.evalBool( new Value(2), _context) );
    assertTrue(  Logic.evalBool( new Value(888), _context) );
    assertTrue(  Logic.evalBool( new Value(-1), _context) );
    assertFalse(  Logic.evalBool( new Value(0), _context) ); //only 0 is false for Numberss
    
    assertFalse( Logic.evalBool( new Value(null), _context) ); //null evals to false
    
    try{
      Logic.evalBool(null, _context);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    try{
      Logic.evalBool(new Value(0), null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
  }
  
  
}














