package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.fleet.model.SourceId;
import romeo.units.api.Acronym;
import romeo.units.impl.UnitImpl;
import romeo.xfactors.expressions.Context.ContextOperand;

public class TestContext {

  private RoundContext _context;
  private FleetElement _earthElementBase; //base fleet for earth (the defender)
  private FleetElement _earthElement1;
  private FleetElement _marsElement0;
  
  @Before
  public void setup() {
    UnitImpl viper = new UnitImpl(null, "Viper", 2, 30, 25, 10, 120, 1, 100, 30, 25, 200, Acronym.fromString("VIP"), null);
    
    _context = new RoundContext(new String[] { "Mars", "Earth" } );
    _context.setDefendingPlayer("Earth");
    _context.setRound(42);
    
    FleetContents marsFleet = new FleetContents();
    _marsElement0 = new FleetElement(viper, 100, SourceId.forBaseOrDefault());
    marsFleet.addElement(_marsElement0);
    _context.setFleet("Mars", marsFleet);
    
    FleetContents earthFleet = new FleetContents();
    _earthElementBase = new FleetElement(viper, 50, SourceId.forBaseOrDefault());
    earthFleet.addElement( _earthElementBase );
    
    _earthElement1 = new FleetElement(viper, 25, SourceId.fromInt(1));;
    earthFleet.addElement( _earthElement1 );
    earthFleet.addElement( new FleetElement(viper, 5, SourceId.fromInt(2)) );
    _context.setFleet("Earth", earthFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(_earthElement1);
  }
  
  @Test
  public void testRound() {
    Context round = new Context(ContextOperand.ROUND);
    assertEquals(42, round.evaluate(_context));
  }
  
  @Test
  public void testIsAttackerDefender() {
    Context attacker = new Context(ContextOperand.IS_ATTACKER);
    _context.setThisPlayer("Mars");
    assertTrue( (Boolean)attacker.evaluate(_context) );
    _context.setThisPlayer("Earth");
    assertFalse( (Boolean)attacker.evaluate(_context) );
    
    Context defender = new Context(ContextOperand.IS_DEFENDER);
    _context.setThisPlayer("Mars");
    assertFalse( (Boolean)defender.evaluate(_context) );
    _context.setThisPlayer("Earth");
    assertTrue( (Boolean)defender.evaluate(_context) );
  }
  
  @Test
  public void testSource() {
    Context source = new Context(ContextOperand.SOURCE);
    //assertEquals( new Integer(1), (Integer)source.evaluate(_context) );
    assertEquals( SourceId.fromInt(1), source.evaluate(_context) );
  }
  
  @Test
  public void testStats() {
    assertEquals( new Integer(2), (Integer)new Context(ContextOperand.ATTACKS).evaluate(_context) );
    assertEquals( new Integer(30), (Integer)new Context(ContextOperand.OFFENSE).evaluate(_context) );
    assertEquals( new Integer(25), (Integer)new Context(ContextOperand.DEFENSE).evaluate(_context) );
  }
  
  @Test
  public void testBase() {
    Context base = new Context(ContextOperand.IS_BASE);
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
    Context pd = new Context(ContextOperand.PD);
    assertEquals(10, pd.evaluate(_context) );
  }
  
}
