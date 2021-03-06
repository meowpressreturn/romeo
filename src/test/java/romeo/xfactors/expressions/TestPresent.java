package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.units.impl.UnitImpl;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestPresent {

  private FleetContents _earthFleet;
  private FleetContents _marsFleet;
  private RoundContext _context;
  
  @Before
  public void setup() {
    UnitImpl viper = new UnitImpl(null, "Viper", 2, 30, 25, 10, 120, 1, 100, 30, 25, 200, "VIP", null);
    
    UnitImpl bstar = new UnitImpl(null, "B.Star", 20, 90, 98, 10, 80, 500, 100, 30, 200, 200, "BS", null);
    
    _context = new RoundContext(new String[] { "Mars", "Earth" } );
    _context.setDefendingPlayer("Earth");
    _context.setRound(42);
    
    _marsFleet = new FleetContents();
    FleetElement marsElement0 = new FleetElement(viper, 100, 0);
    _marsFleet.addElement(marsElement0);
    _marsFleet.addElement(new FleetElement(bstar, 2, 1));
    _context.setFleet("Mars", _marsFleet);
    
    _earthFleet = new FleetContents();
    FleetElement earthElementBase = new FleetElement(viper, 50, 0);
    _earthFleet.addElement( earthElementBase );
    
    FleetElement earthElement1 = new FleetElement(viper, 25, 1);;
    _earthFleet.addElement( earthElement1 );
    _earthFleet.addElement( new FleetElement(viper, 5, 2) );
    _context.setFleet("Earth", _earthFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(earthElement1);
  }
  
  @Test
  public void testConstructor() {
    Present present = new Present("VIP");
    assertEquals( "VIP", present.getAcronym() );
    
    try {
      new Present(null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    try {
      new Present("");
      fail("Expected IllegalArgumentsException");
    }catch(IllegalArgumentException expected) {}
    
    new Present("any text here should be ok");
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();
    
    assertEquals("VIP", new Present("VIP",parser).getAcronym() );
    assertEquals("VIP", new Present("   VIP\n  ",parser).getAcronym() );
    
    try {
      new Present("foo,bar,baz", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Present("", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Present(",", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Present(",,,,,,,", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Present("VIP,BS", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Present("    ", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Present("\"\"", parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    new Present(" \"    \"   ", parser); //this is silly but allowed
    
    try {
      new Present(null,parser);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    try {
      new Present("foo",null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    assertEquals("VIP", new Present("\"VIP\"",parser).getAcronym() );
    assertEquals("VIP", new Present("    \"VIP\" ",parser).getAcronym() );
    assertEquals(" VIP ", new Present("\" VIP \"",parser).getAcronym() );
  }
  
  @Test
  public void testEvaluation() {
    
    _context.setThisPlayer("Mars");
    
    assertTrue( (Boolean)new Present("VIP").evaluate(_context) );
    assertTrue( (Boolean)new Present("BS").evaluate(_context) );
    assertFalse( (Boolean)new Present("XWNG").evaluate(_context) );
    //the evaluation should be case-insenstive
    assertTrue( (Boolean)new Present("vip").evaluate(_context) );
    assertTrue( (Boolean)new Present("Vip").evaluate(_context) );
    
    _context.setThisPlayer("Earth");
    assertTrue( (Boolean)new Present("VIP").evaluate(_context) );
    assertFalse( (Boolean)new Present("BS").evaluate(_context) );
    assertFalse( (Boolean)new Present("XWNG").evaluate(_context) );
  }
  
  @Test
  public void testUnitPresent() {    
    assertTrue( Present.unitPresent(_earthFleet, "VIP") );
    assertTrue( Present.unitPresent(_earthFleet, "vip") );
    assertTrue( Present.unitPresent(_earthFleet, "vIP") );    
    assertFalse( Present.unitPresent(_earthFleet, "BS") );
    assertTrue( Present.unitPresent(_marsFleet, "VIP") );
    assertTrue( Present.unitPresent(_marsFleet, "BS") );
    
    try {
      Present.unitPresent(null, "med");
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    try {
      Present.unitPresent(_earthFleet, null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    Present.unitPresent(_earthFleet, ""); //probably useless but allowed
  }
  
  
}
