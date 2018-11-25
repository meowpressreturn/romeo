package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.fleet.model.SourceId;
import romeo.units.api.IUnit;
import romeo.units.impl.TestUnitImpl;
import romeo.xfactors.expressions.Quantity.QuantityOperand;

public class TestQuantity {
  
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
    FleetElement marsElement0 = new FleetElement(viper, 100, SourceId.forBaseOrDefault());
    _marsFleet.addElement(marsElement0);
    _marsFleet.addElement(new FleetElement(bstar, 2, SourceId.fromInt(1)));
    _context.setFleet("Mars", _marsFleet);
    
    
    //earth commands 80 vipers in 3 squadrons, and no bstars
    //I think earth is kinda screwed here
    _earthFleet = new FleetContents();
    FleetElement earthElementBase = new FleetElement(viper, 50, SourceId.forBaseOrDefault());
    _earthFleet.addElement( earthElementBase );
    
    FleetElement earthElement1 = new FleetElement(viper, 25, SourceId.fromInt(1));
    _earthFleet.addElement( earthElement1 );
    _earthFleet.addElement( new FleetElement(viper, 5, SourceId.fromInt(2)) );
    _context.setFleet("Earth", _earthFleet);
    
    //venus has only a single basestar (ironically not in base fleet), so they aren't looking good either
    //although having it in fleet 888 might help a little maybe? good luck
    _venusFleet = new FleetContents();
    _venusFleet.addElement(new FleetElement(bstar,1,SourceId.fromInt(888)) );
    _context.setFleet("Venus",_venusFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(earthElement1);
    
    
    
  }
  
  @Test
  public void testConstructor() {
    
    Quantity q = new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.forBaseOrDefault());
    assertEquals( QuantityOperand.THIS_PLAYER, q.getOperand() );
    assertEquals( 0, q.getSourceId().asInteger().intValue());
    assertEquals("VIP", q.getAcronym());
    
    new Quantity(QuantityOperand.ANY_PLAYER, "VIP", SourceId.forBaseOrDefault());
    new Quantity(QuantityOperand.OPPOSING_PLAYERS, "VIP", SourceId.forBaseOrDefault());
    new Quantity(QuantityOperand.ANY_PLAYER, "VIP", SourceId.forAnySource()); 
    
    Quantity q2 = new Quantity(QuantityOperand.OPPOSING_PLAYERS, "VIP", SourceId.forAnySource());
    assertEquals( SourceId.forAnySource(), q2.getSourceId() );
    
    try {
      new Quantity(QuantityOperand.THIS_PLAYER, null, SourceId.forBaseOrDefault());
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Quantity(null, "foo", SourceId.forBaseOrDefault());
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      //Now that SourceId has be reified it may not be null
      new Quantity(QuantityOperand.THIS_PLAYER, "foo", null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
        
  }
  
  @Test
  public void testEvaluate() {
    _context.setThisPlayer("Mars");
    assertEquals( new Double(100), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.forAnySource()).evaluate(_context) );
    assertEquals( new Double(100), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.forBaseOrDefault()).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.fromInt(1)).evaluate(_context) );
    assertEquals( new Double(80), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "VIP", SourceId.forAnySource()).evaluate(_context) );
    assertEquals( new Double(5), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "VIP", SourceId.fromInt(2)).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "VIP", SourceId.fromInt(888)).evaluate(_context) );
    
    _context.setThisPlayer("Earth");
    assertEquals( new Double(80), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.forAnySource()).evaluate(_context) );
    assertEquals( new Double(50), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.forBaseOrDefault()).evaluate(_context) );
    assertEquals( new Double(25), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.fromInt(1)).evaluate(_context) );
    assertEquals( new Double(3), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "BS", SourceId.forAnySource()).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "BS", SourceId.forBaseOrDefault()).evaluate(_context) );
    assertEquals( new Double(2), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "BS", SourceId.fromInt(1)).evaluate(_context) ); //martian bs
    assertEquals( new Double(1), new Quantity(QuantityOperand.OPPOSING_PLAYERS, "BS", SourceId.fromInt(888)).evaluate(_context) ); //venusian lucky bs
    
    _context.setThisPlayer("Venus");
    assertEquals( new Double(0), new Quantity(QuantityOperand.THIS_PLAYER, "VIP", SourceId.forAnySource()).evaluate(_context) );
    assertEquals( new Double(0), new Quantity(QuantityOperand.THIS_PLAYER, "MED", SourceId.forAnySource()).evaluate(_context) );
    assertEquals( new Double(1), new Quantity(QuantityOperand.THIS_PLAYER, "BS", SourceId.forAnySource()).evaluate(_context) );
  }
  

  
}























