package romeo.fleet.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.units.api.IUnit;
import romeo.units.api.UnitId;
import romeo.units.impl.TestUnitImpl;

public class TestFleetContents {

  private static final Double D = 0.000000001;
  
  private FleetContents _earthFleet;
  private FleetContents _marsFleet;
  private FleetContents _venusFleet;
  private FleetContents _testFleet;
  private RoundContext _context; //can probably remove this (we copied all this over from Quantity...)
  
  @Before
  public void setup() {
    IUnit viper = TestUnitImpl.newVip();    
    IUnit bstar = TestUnitImpl.newBStar();
    IUnit rap = TestUnitImpl.newRap(new UnitId("RAP"));
    
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
    
    
    //For testing normalisations
    _testFleet = new FleetContents();
    _testFleet.addElement( new FleetElement( viper, 100, SourceId.fromInt(0)) );
    _testFleet.addElement( new FleetElement( viper, 200, SourceId.fromInt(1)) ); //same as above
    _testFleet.addElement( new FleetElement( viper, 50, SourceId.fromInt(1)) ); //same as above
    _testFleet.addElement( new FleetElement( bstar, 1, SourceId.fromInt(0)) );
    _testFleet.addElement( new FleetElement( bstar, 3, SourceId.fromInt(0)) ); //same as above
    _testFleet.addElement( new FleetElement( rap, 6, SourceId.fromInt(0)) );
    //so, all up flat: 350 * VIP, 4 * BS, 6 * RAP, 
    //or with source: 100 * VIP, 1:150*VIP, 4 * BS, 6 * RAP
    
  }
  
  @Test
  public void testGetQuantity() {
    
    assertEquals( 0, _venusFleet.getQuantity("MED", SourceId.forAnySource() ), D );
    assertEquals( 1, _venusFleet.getQuantity("BS", SourceId.forAnySource() ), D );
    assertEquals( 1, _venusFleet.getQuantity( "BS", SourceId.fromInt(888) ), D );
    assertEquals( 0, _venusFleet.getQuantity("BS", SourceId.forBaseOrDefault() ), D );
    assertEquals( 0, _venusFleet.getQuantity("BS", SourceId.fromInt(1) ), D );    
    
    assertEquals( 80, _earthFleet.getQuantity("VIP", SourceId.forAnySource() ), D );
    assertEquals( 50, _earthFleet.getQuantity("VIP", SourceId.forBaseOrDefault() ), D );
    assertEquals( 25, _earthFleet.getQuantity("VIP", SourceId.fromInt(1) ), D );
    assertEquals( 5, _earthFleet.getQuantity("VIP", SourceId.fromInt(2) ), D );
    assertEquals( 0, _earthFleet.getQuantity("VIP", SourceId.fromInt(3) ), D );
    
    assertEquals( 100, _marsFleet.getQuantity("VIP", SourceId.forAnySource() ), D );
    assertEquals( 0, _marsFleet.getQuantity("MED", SourceId.forAnySource() ), D );
    assertEquals( 0, _marsFleet.getQuantity("MED", SourceId.fromInt(1) ), D );
    
  }
  
  @Test
  public void testUnitPresent() {    
    assertTrue( _earthFleet.unitPresent("VIP") );
    assertTrue( _earthFleet.unitPresent("vip") );
    assertTrue( _earthFleet.unitPresent("vIP") );    
    assertFalse( _earthFleet.unitPresent("BS") );
    assertTrue( _marsFleet.unitPresent("VIP") );
    assertTrue( _marsFleet.unitPresent("BS") );

    try {
      _marsFleet.unitPresent(null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    _marsFleet.unitPresent(""); //probably useless but allowed (for now)
  }
  
  
  @Test
  public void testNormalisation() {
   
    FleetContents fleet = _testFleet;
    
    assertEquals( "Element count before normalisation", 4, fleet.getElements().size() ); //ALREADY normalised!
    assertEquals( 350, fleet.getQuantity("VIP", SourceId.forAnySource()), D );
    assertEquals( 4, fleet.getQuantity("BS", SourceId.forAnySource()), D );
    assertEquals( 6, fleet.getQuantity("RAP", SourceId.forAnySource()), D );
    
    FleetContents b = new FleetContents();
    b.addElement( new FleetElement(TestUnitImpl.newBStar(), 4, SourceId.fromInt(0))  );
    b.addElement( new FleetElement(TestUnitImpl.newVip(), 100, SourceId.fromInt(0))  );
    fleet.addFleet(b, false);
    
    assertEquals( "Element count before normalisation after adding b", 6, fleet.getElements().size() );
    assertEquals( 8, fleet.getQuantity("BS", SourceId.forAnySource()), D );
    assertEquals( 450, fleet.getQuantity("VIP", SourceId.forAnySource()), D );
    
    //can we denormalise it here somehow?
    
    fleet.normalise(false);
    assertEquals( "Element count after normalisation", 4, fleet.getElements().size() );
    assertEquals( 450, fleet.getQuantity("VIP", SourceId.forAnySource()), D );
    assertEquals( 8, fleet.getQuantity("BS", SourceId.forAnySource()), D );
    assertEquals( 6, fleet.getQuantity("RAP", SourceId.forAnySource()), D );
    
  }
  
}



















