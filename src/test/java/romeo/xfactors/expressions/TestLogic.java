package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.xfactors.expressions.Logic.LogicOperand;

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
    Logic logic = new Logic( left, LogicOperand.AND, right );
    assertEquals( left, logic.getLeft() );
    assertEquals( LogicOperand.AND, logic.getOperand() );
    assertEquals( right, logic.getRight() );
    
    try {
      new Logic(null, LogicOperand.AND, right);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Logic(left, LogicOperand.AND, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Logic(left, null, right);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testAnd() {
    assertTrue( (Boolean)new Logic(new Value(true),LogicOperand.AND,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.AND,new Value(false)).evaluate(_context) );   
    assertFalse( (Boolean)new Logic(new Value(true),LogicOperand.AND,new Value(false)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.AND,new Value(true)).evaluate(_context) );
  }
  
  @Test
  public void testOr() {
    assertTrue( (Boolean)new Logic(new Value(true),LogicOperand.OR,new Value(true)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(true),LogicOperand.OR,new Value(false)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(false),LogicOperand.OR,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.OR,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testXor() {
    assertFalse( (Boolean)new Logic(new Value(true),LogicOperand.XOR,new Value(true)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(true),LogicOperand.XOR,new Value(false)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(false),LogicOperand.XOR,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.XOR,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testNor() {
    assertFalse( (Boolean)new Logic(new Value(true),LogicOperand.NOR,new Value(true)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(true),LogicOperand.NOR,new Value(false)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.NOR,new Value(true)).evaluate(_context) );    
    assertTrue( (Boolean)new Logic(new Value(false),LogicOperand.NOR,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testNot() {
    //nb: like the other operands, this one is working on boolean left and right values
    assertFalse( (Boolean)new Logic(new Value(true),LogicOperand.NOT,new Value(true)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(true),LogicOperand.NOT,new Value(false)).evaluate(_context) );
    assertTrue( (Boolean)new Logic(new Value(false),LogicOperand.NOT,new Value(true)).evaluate(_context) );    
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.NOT,new Value(false)).evaluate(_context) ); 
  }
  
  @Test
  public void testEqual() {
    //nb: like the other operands, this one is working on boolean left and right values
    assertTrue( (Boolean)new Logic(new Value(true),LogicOperand.EQUAL,new Value(true)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(true),LogicOperand.EQUAL,new Value(false)).evaluate(_context) );
    assertFalse( (Boolean)new Logic(new Value(false),LogicOperand.EQUAL,new Value(true)).evaluate(_context) );    
    assertTrue( (Boolean)new Logic(new Value(false),LogicOperand.EQUAL,new Value(false)).evaluate(_context) ); 
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














