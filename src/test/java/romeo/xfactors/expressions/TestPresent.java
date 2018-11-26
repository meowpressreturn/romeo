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

public class TestPresent {

  private FleetContents _earthFleet;
  private FleetContents _marsFleet;
  private RoundContext _context;
  
  @Before
  public void setup() {
    UnitImpl viper = new UnitImpl(null, "Viper", 2, 30, 25, 10, 120, 1, 100, 30, 25, 200, Acronym.fromString("VIP"), null);
    
    UnitImpl bstar = new UnitImpl(null, "B.Star", 20, 90, 98, 10, 80, 500, 100, 30, 200, 200, Acronym.fromString("BS"), null);
    
    _context = new RoundContext(new String[] { "Mars", "Earth" } );
    _context.setDefendingPlayer("Earth");
    _context.setRound(42);
    
    _marsFleet = new FleetContents();
    FleetElement marsElement0 = new FleetElement(viper, 100, SourceId.forBaseOrDefault());
    _marsFleet.addElement(marsElement0);
    _marsFleet.addElement(new FleetElement(bstar, 2, SourceId.fromInt(1)));
    _context.setFleet("Mars", _marsFleet);
    
    _earthFleet = new FleetContents();
    FleetElement earthElementBase = new FleetElement(viper, 50, SourceId.forBaseOrDefault());
    _earthFleet.addElement( earthElementBase );
    
    FleetElement earthElement1 = new FleetElement(viper, 25, SourceId.fromInt(1));;
    _earthFleet.addElement( earthElement1 );
    _earthFleet.addElement( new FleetElement(viper, 5, SourceId.fromInt(2)) );
    _context.setFleet("Earth", _earthFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(earthElement1);
  }
  
  @Test
  public void testConstructor() {
    Present present = new Present( Acronym.fromString("VIP") );
    assertEquals( Acronym.fromString("VIP"), present.getAcronym() );
    
    try {
      new Present(null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testEvaluation() {
    
    _context.setThisPlayer("Mars");
    
    Acronym vip = Acronym.fromString("VIP");
    Acronym bs = Acronym.fromString("BS");
    Acronym xwng = Acronym.fromString("XWNG");
    
    assertTrue( (Boolean)new Present(vip).evaluate(_context) );
    assertTrue( (Boolean)new Present(bs).evaluate(_context) );
    assertFalse( (Boolean)new Present(xwng).evaluate(_context) );
    
    _context.setThisPlayer("Earth");
    assertTrue( (Boolean)new Present(vip).evaluate(_context) );
    assertFalse( (Boolean)new Present(bs).evaluate(_context) );
    assertFalse( (Boolean)new Present(xwng).evaluate(_context) );
  }
  
  
  
  
}
