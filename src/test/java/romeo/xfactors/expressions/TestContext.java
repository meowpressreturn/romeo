package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.units.impl.UnitImpl;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestContext {

  private RoundContext _context;
  private FleetElement _earthElementBase; //base fleet for earth (the defender)
  private FleetElement _earthElement1;
  private FleetElement _marsElement0;
  
  @Before
  public void setup() {
    UnitImpl viper = new UnitImpl(null, "Viper", 2, 30, 25, 10, 120, 1, 100, 30, 25, 200, "VIP", null);
    
    _context = new RoundContext(new String[] { "Mars", "Earth" } );
    _context.setDefendingPlayer("Earth");
    _context.setRound(42);
    
    FleetContents marsFleet = new FleetContents();
    _marsElement0 = new FleetElement(viper, 100, 0);
    marsFleet.addElement(_marsElement0);
    _context.setFleet("Mars", marsFleet);
    
    FleetContents earthFleet = new FleetContents();
    _earthElementBase = new FleetElement(viper, 50, 0);
    earthFleet.addElement( _earthElementBase );
    
    _earthElement1 = new FleetElement(viper, 25, 1);;
    earthFleet.addElement( _earthElement1 );
    earthFleet.addElement( new FleetElement(viper, 5, 2) );
    _context.setFleet("Earth", earthFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(_earthElement1);
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();
    IExpressionTokeniser tokeniser = new ExpressionParserImpl();
    assertEquals( Context.IS_DEFENDER, new Context("IS_DEFENDER",parser, tokeniser).getOperand() );
    assertEquals( Context.IS_ATTACKER, new Context("IS_ATTACKER",parser, tokeniser).getOperand() );
    assertEquals( Context.SOURCE, new Context("SOURCE",parser, tokeniser).getOperand() );
    assertEquals( Context.ATTACKS, new Context("ATTACKS",parser, tokeniser).getOperand() );
    assertEquals( Context.OFFENSE, new Context("OFFENSE",parser, tokeniser).getOperand() );
    assertEquals( Context.DEFENSE, new Context("DEFENSE",parser, tokeniser).getOperand() );
    assertEquals( Context.IS_BASE, new Context("IS_BASE",parser, tokeniser).getOperand() );
    assertEquals( Context.IS_NOT_BASE, new Context("IS_NOT_BASE",parser, tokeniser).getOperand() );
    assertEquals( Context.PD, new Context("PD",parser, tokeniser).getOperand() );    
    
    try {
      new Context("NOSUCHOP", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      new Context("", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      new Context(",,,,", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      new Context("SOURCE,OFFENSE", parser, tokeniser);
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    try {
      new Context(null, parser, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Context("PD",null, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Context("PD",parser, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testRound() {
    Context round = new Context(Context.ROUND);
    assertEquals(42, round.evaluate(_context));
  }
  
  @Test
  public void testIsAttackerDefender() {
    Context attacker = new Context(Context.IS_ATTACKER);
    _context.setThisPlayer("Mars");
    assertTrue( (Boolean)attacker.evaluate(_context) );
    _context.setThisPlayer("Earth");
    assertFalse( (Boolean)attacker.evaluate(_context) );
    
    Context defender = new Context(Context.IS_DEFENDER);
    _context.setThisPlayer("Mars");
    assertFalse( (Boolean)defender.evaluate(_context) );
    _context.setThisPlayer("Earth");
    assertTrue( (Boolean)defender.evaluate(_context) );
  }
  
  @Test
  public void testSource() {
    Context source = new Context(Context.SOURCE);
    assertEquals( new Integer(1), (Integer)source.evaluate(_context) );
  }
  
  @Test
  public void testStats() {
    assertEquals( new Integer(2), (Integer)new Context(Context.ATTACKS).evaluate(_context) );
    assertEquals( new Integer(30), (Integer)new Context(Context.OFFENSE).evaluate(_context) );
    assertEquals( new Integer(25), (Integer)new Context(Context.DEFENSE).evaluate(_context) );
  }
  
  @Test
  public void testBase() {
    Context base = new Context(Context.IS_BASE);
    _context.setThisPlayer("Earth");
    _context.setFleetElement(_earthElementBase);
    assertTrue( (Boolean)base.evaluate(_context) );
    _context.setFleetElement(_earthElement1);
    assertFalse( (Boolean)base.evaluate(_context) );
    _context.setThisPlayer("Mars");
    _context.setFleetElement(_marsElement0); //as the attacker this isnt base even though is zero
    assertFalse( (Boolean)base.evaluate(_context) );
  }
  
  @Test
  public void testPd() {
    Context pd = new Context(Context.PD);
    assertEquals(10, pd.evaluate(_context) );
  }
  
}
