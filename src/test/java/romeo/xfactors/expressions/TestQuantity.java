package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.units.api.IUnit;
import romeo.units.impl.TestUnitImpl;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestQuantity {
  
  private static final Double D = 0.000000001;
  
  private FleetContents _earthFleet;
  private FleetContents _marsFleet;
  private FleetContents _venusFleet;
  private RoundContext _context;
  
  @Before
  public void setup() {
    IUnit viper = TestUnitImpl.newVip();
    
    IUnit bstar = TestUnitImpl.newBStar();
    
    //mars has 100 vipers in one squad, and a couple of basestars (or battlestars - you decide!) in another
    _context = new RoundContext(new String[] { "Mars", "Earth", "Venus" } );
    _context.setDefendingPlayer("Earth");
    _context.setRound(42);
    
    _marsFleet = new FleetContents();
    FleetElement marsElement0 = new FleetElement(viper, 100, 0);
    _marsFleet.addElement(marsElement0);
    _marsFleet.addElement(new FleetElement(bstar, 2, 1));
    _context.setFleet("Mars", _marsFleet);
    
    
    //earth commands 80 vipers in 3 squadrons, and no bstars
    //I think earth is kinda screwed here
    _earthFleet = new FleetContents();
    FleetElement earthElementBase = new FleetElement(viper, 50, 0);
    _earthFleet.addElement( earthElementBase );
    
    FleetElement earthElement1 = new FleetElement(viper, 25, 1);
    _earthFleet.addElement( earthElement1 );
    _earthFleet.addElement( new FleetElement(viper, 5, 2) );
    _context.setFleet("Earth", _earthFleet);
    
    //venus has only a single basestar (ironically not in base fleet), so they aren't looking good either
    //although having it in fleet 888 might help a little maybe? good luck
    _venusFleet = new FleetContents();
    _venusFleet.addElement(new FleetElement(bstar,1,888) );
    _context.setFleet("Venus",_venusFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(earthElement1);
    
    
    
  }
  
  @Test
  public void testConstructor() {
    
    Quantity q = new Quantity(Quantity.THIS_PLAYER, "VIP", 0);
    assertEquals( Quantity.THIS_PLAYER, q.getOperand() );
    assertEquals( new Integer(0), q.getSourceId());
    assertEquals("VIP", q.getAcronym());
    
    new Quantity(Quantity.ANY_PLAYER, "VIP", 0);
    new Quantity(Quantity.OPPOSING_PLAYERS, "VIP", 0);
    new Quantity(Quantity.ANY_PLAYER, "VIP", null); //null source is quote legal (means use any source)
    
    //null sourceId means it can be present in any source for that player
    Quantity q2 = new Quantity(Quantity.OPPOSING_PLAYERS, "VIP", null);
    assertNull( q2.getSourceId() );
    
    try {
      new Quantity(Quantity.THIS_PLAYER, null, 0);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Quantity(888, "foo", 0);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
        
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();

    assertEquals( Quantity.ANY_PLAYER, new Quantity("ANY_PLAYER,MED,0",parser).getOperand() );
    assertEquals( Quantity.OPPOSING_PLAYERS, new Quantity("OPPOSING_PLAYERS,MED,0",parser).getOperand() );
    assertEquals( Quantity.THIS_PLAYER, new Quantity("THIS_PLAYER,MED,0",parser).getOperand() );
    
    assertEquals( new Integer(0), new Quantity("ANY_PLAYER,MED,0",parser).getSourceId() );
    assertEquals( new Integer(1), new Quantity("ANY_PLAYER,MED,1",parser).getSourceId() );
    assertEquals( new Integer(888), new Quantity("ANY_PLAYER,MED,888",parser).getSourceId() ); //high sourceIds are fine
    
    assertNull( new Quantity("ANY_PLAYER,MED,NULL",parser).getSourceId() );
    assertNull( new Quantity("ANY_PLAYER,MED,null",parser).getSourceId() );
    
    try {
      Quantity q = new Quantity("ANY_PLAYER,MED,-1",parser); //negative sourceId isnt allowed
      fail("Expected IllegalArgumentException but found " + q);
      //in hindsight, using -1 for any source would have been better and could then use a primitive for it
      //and avoid nullskullduggery
    }catch(IllegalArgumentException expected) {}
    
    
    try {
      new Quantity(null, parser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Quantity("ANY_PLAYER,MED,0", null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Quantity("",parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Quantity(",,,,,,,,",parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Quantity(",,,",parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Quantity("ANY_PLAYER,MED,0,1",parser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
  }
  
  @Test
  public void testEvaluate() {
    _context.setThisPlayer("Mars");
    assertEquals( new Double(100), new Quantity(Quantity.THIS_PLAYER, "VIP", null).evaluate(_context) );
    assertEquals( new Double(100), new Quantity(Quantity.THIS_PLAYER, "VIP", 0).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(Quantity.THIS_PLAYER, "VIP", 1).evaluate(_context) );
    assertEquals( new Double(80), new Quantity(Quantity.OPPOSING_PLAYERS, "VIP", null).evaluate(_context) );
    assertEquals( new Double(5), new Quantity(Quantity.OPPOSING_PLAYERS, "VIP", 2).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(Quantity.OPPOSING_PLAYERS, "VIP", 888).evaluate(_context) );
    
    _context.setThisPlayer("Earth");
    assertEquals( new Double(80), new Quantity(Quantity.THIS_PLAYER, "VIP", null).evaluate(_context) );
    assertEquals( new Double(50), new Quantity(Quantity.THIS_PLAYER, "VIP", 0).evaluate(_context) );
    assertEquals( new Double(25), new Quantity(Quantity.THIS_PLAYER, "VIP", 1).evaluate(_context) );
    assertEquals( new Double(3), new Quantity(Quantity.OPPOSING_PLAYERS, "BS", null).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(Quantity.OPPOSING_PLAYERS, "BS", 0).evaluate(_context) );
    assertEquals( new Double(2), new Quantity(Quantity.OPPOSING_PLAYERS, "BS", 1).evaluate(_context) ); //martian bs
    assertEquals( new Double(1), new Quantity(Quantity.OPPOSING_PLAYERS, "BS", 888).evaluate(_context) ); //venusian lucky bs
    
    _context.setThisPlayer("Venus");
    assertEquals( new Double(0), new Quantity(Quantity.THIS_PLAYER, "VIP", null).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(Quantity.THIS_PLAYER, "MED", null).evaluate(_context) );
    assertEquals( new Double(1), new Quantity(Quantity.THIS_PLAYER, "BS", null).evaluate(_context) );
  }
  
  @Test
  public void testGetQuantity() {
    
    assertEquals( 0, Quantity.getQuantity(_venusFleet, "MED", null ), D );
    assertEquals( 1, Quantity.getQuantity(_venusFleet, "BS", null ), D );
    assertEquals( 1, Quantity.getQuantity(_venusFleet, "BS", 888 ), D );
    assertEquals( 0, Quantity.getQuantity(_venusFleet, "BS", 0 ), D );
    assertEquals( 0, Quantity.getQuantity(_venusFleet, "BS", 1 ), D );    
    
    assertEquals( 80, Quantity.getQuantity(_earthFleet, "VIP", null ), D );
    assertEquals( 50, Quantity.getQuantity(_earthFleet, "VIP", 0 ), D );
    assertEquals( 25, Quantity.getQuantity(_earthFleet, "VIP", 1 ), D );
    assertEquals( 5, Quantity.getQuantity(_earthFleet, "VIP", 2 ), D );
    assertEquals( 0, Quantity.getQuantity(_earthFleet, "VIP", 3 ), D );
    
    assertEquals( 100, Quantity.getQuantity(_marsFleet, "VIP", null ), D );
    assertEquals( 0, Quantity.getQuantity(_earthFleet, "MED", null ), D );
    assertEquals( 0, Quantity.getQuantity(_earthFleet, "MED", 1 ), D );
    
  }
  
}























