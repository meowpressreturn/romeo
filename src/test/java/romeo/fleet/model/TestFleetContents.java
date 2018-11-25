package romeo.fleet.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.units.api.IUnit;
import romeo.units.impl.TestUnitImpl;

public class TestFleetContents {

  private static final Double D = 0.000000001;
  
  private FleetContents _earthFleet;
  private FleetContents _marsFleet;
  private FleetContents _venusFleet;
  private RoundContext _context; //can probably remove this (we copied all this over from Quantity...)
  
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
  public void testGetQuantity() {
    
    assertEquals( 0, _venusFleet.getQuantity("MED", null ), D );
    assertEquals( 1, _venusFleet.getQuantity("BS", null ), D );
    assertEquals( 1, _venusFleet.getQuantity( "BS", 888 ), D );
    assertEquals( 0, _venusFleet.getQuantity("BS", 0 ), D );
    assertEquals( 0, _venusFleet.getQuantity("BS", 1 ), D );    
    
    assertEquals( 80, _earthFleet.getQuantity("VIP", null ), D );
    assertEquals( 50, _earthFleet.getQuantity("VIP", 0 ), D );
    assertEquals( 25, _earthFleet.getQuantity("VIP", 1 ), D );
    assertEquals( 5, _earthFleet.getQuantity("VIP", 2 ), D );
    assertEquals( 0, _earthFleet.getQuantity("VIP", 3 ), D );
    
    assertEquals( 100, _marsFleet.getQuantity("VIP", null ), D );
    assertEquals( 0, _marsFleet.getQuantity("MED", null ), D );
    assertEquals( 0, _marsFleet.getQuantity("MED", 1 ), D );
    
  }
  
}



















