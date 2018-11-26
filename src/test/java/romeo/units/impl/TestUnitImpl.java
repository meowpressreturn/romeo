package romeo.units.impl;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import romeo.units.api.Acronym;
import romeo.units.api.IUnit;
import romeo.units.api.UnitId;
import romeo.xfactors.api.XFactorId;

/**
 * Tests for the {@link UnitImpl} class. This also includes some static helper methods that are of use to other tests
 * that need to work with units, most notably {@link TestUnitServiceImpl}
 */
public class TestUnitImpl {
  
  public static IUnit newBsr(UnitId id) {
    return new UnitImpl(id, "Big Shooty Robot", 30, 50, 96, 200, 180, 0, 1000, 1500, 0, 2000, Acronym.fromString("BSR"), null);
  }
  
  public static IUnit newRap(UnitId id) {
    IUnit rap = new UnitImpl(id, "Recon", 3, 20, 35, 5, 100, 30, 100, 250, 200, 500, Acronym.fromString("RAP"), null);
    assertRapCorrect(id, rap); //sanity check
    return rap;
  }

  public static void assertRapCorrect(UnitId id, IUnit rap) {
    assertNotNull(rap);
    assertEquals(id, rap.getId());
    assertEquals("Recon", rap.getName());
    assertEquals(3, rap.getAttacks());
    assertEquals(20, rap.getOffense());
    assertEquals(35, rap.getDefense());
    assertEquals(5, rap.getPd());
    assertEquals(100, rap.getSpeed());
    assertEquals(30, rap.getCarry());
    assertEquals(100,rap.getCost());
    assertEquals(250, rap.getComplexity());
    assertEquals(200, rap.getScanner());
    assertEquals(500, rap.getLicense());
    assertEquals(Acronym.fromString("RAP"),rap.getAcronym());
    assertNull(rap.getXFactor());
  }
  
  public static UnitImpl newBStar() {
    return new UnitImpl(null, "Carrier", 20, 90, 98, 10, 80, 500, 2000, 1800, 100, 2000, Acronym.fromString("BS"), null);
  }

  public static void assertBsCorrect(UnitId id, IUnit bs) {
    assertNotNull(bs);
    assertEquals( id, bs.getId() );
    assertEquals("Carrier", bs.getName());
    assertEquals(20, bs.getAttacks());
    assertEquals(90, bs.getOffense());
    assertEquals(98, bs.getDefense());
    assertEquals(10, bs.getPd());
    assertEquals(80, bs.getSpeed());
    assertEquals(500, bs.getCarry());
    assertEquals(2000, bs.getCost());
    assertEquals(1800, bs.getComplexity());
    assertEquals(100, bs.getScanner());
    assertEquals(2000, bs.getLicense());
    assertEquals(Acronym.fromString("BS"), bs.getAcronym());
    assertNull( bs.getXFactor() );
  }
  
  /**
   * Returns a new test unit with some well known (to our tests) data. nb: The acronym is "vip"
   * @return
   */
  public static UnitImpl newVip() {
    return new UnitImpl(null, "Fighter", 1, 20, 25, 2, 120, 1, 100, 30, 25, 200, Acronym.fromString("vip"), new XFactorId("XF1"));
  }

  /**
   * Check the supplied unit has the correct test data for the VIP example unit
   * @param vip
   */
  public static void assertVipCorrect(UnitId id, IUnit vip) {
    assertNotNull(vip);
    assertEquals(id,vip.getId());
    assertEquals("Fighter",vip.getName());    
    assertEquals(1, vip.getAttacks());
    assertEquals(20, vip.getOffense());
    assertEquals(25, vip.getDefense());
    assertEquals(2, vip.getPd());
    assertEquals(120, vip.getSpeed());
    assertEquals(1, vip.getCarry());
    assertEquals(100, vip.getCost());
    assertEquals(30, vip.getComplexity());
    assertEquals(25, vip.getScanner());
    assertEquals(200, vip.getLicense());
    assertEquals(Acronym.fromString("VIP"),vip.getAcronym());
    assertEquals(new XFactorId("XF1"), vip.getXFactor());
  }
  
  /**
   * Creates a new unit with a single property changed.
   * @param unit
   * @param property
   * @param value
   * @return
   */
  public static UnitImpl mutate(IUnit unit, String property, Object value) {
    Map<String,Object> map = UnitImpl.asMap(unit);
    map.put(property,value);
    return UnitImpl.createFromMap(map);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  
  @Test
  public void testCalcFirepower() {
    
    double delta = 0.01; //only compare to 2 decimal places (as fp is a display-only thing displayed with 2dp)
    
    //can you guess which units these are without looking at the help?
    assertEquals( 10.95, UnitImpl.calcFirepower(12, 50, 95) , delta);
    assertEquals( 0.37, UnitImpl.calcFirepower(1,12,10) , delta);
    assertEquals( 6.12, UnitImpl.calcFirepower(10,15,96) , delta);
    assertEquals( 34.64, UnitImpl.calcFirepower(30,40,99) , delta);
    assertEquals( 0.14, UnitImpl.calcFirepower(1,1,50) , delta);
    assertEquals( 22.36, UnitImpl.calcFirepower(25,40,98) , delta);
    assertEquals( 50.99, UnitImpl.calcFirepower(40,65,99) , delta);
    
    //non-coms
    assertEquals( 0d, UnitImpl.calcFirepower(0,0,0) , delta);    
    
    //a non-com is any unit with a 0 attack or defense or offense
    assertEquals( 0d, UnitImpl.calcFirepower(100,100,0) , delta);   
    assertEquals( 0d, UnitImpl.calcFirepower(0,100,100) , delta);   
    assertEquals( 0d, UnitImpl.calcFirepower(100,0,100) , delta);   
    
    //for simplicity, we just treat negatives as being 0 too
    assertEquals( 0d, UnitImpl.calcFirepower(100,100,-1) , delta);   
    assertEquals( 0d, UnitImpl.calcFirepower(-1,100,100) , delta);   
    assertEquals( 0d, UnitImpl.calcFirepower(100,-1,100) , delta);   
  }
  
  @Test
  public void testConstructor() {
    UnitImpl vip = newVip();
    assertVipCorrect(null, vip);
    
    //test the copying constructor
    UnitImpl anotherVip = new UnitImpl(new UnitId("1234"), vip);
    assertVipCorrect(new UnitId("1234"), anotherVip);
  }
  
  /**
   * Test some round trips between asMap and createFromMap to verify all the required
   * properties got copied. 
   */
  @Test
  public void testAsMap_CreateFromMap() {
    
    IUnit vip = TestUnitImpl.newVip();
    Map<String,Object> map = UnitImpl.asMap(vip); 
    map.put("id", new UnitId("ABC123"));
    map.put("otherJunk","blah");
    map.put("foo","bar");
    UnitImpl unit = UnitImpl.createFromMap(map);
    assertVipCorrect(new UnitId("ABC123"), unit);
    
    map.remove("id");
    UnitImpl unit2 = UnitImpl.createFromMap(map);
    assertVipCorrect(null, unit2);
    
    map.remove("xFactor");
    UnitImpl unit3 = UnitImpl.createFromMap(map);
    assertNull( unit3.getXFactor() ); 
  }

  @Test
  public void testGeneratePlaceholderAcronym() {
    String[][] tests = new String[][] { 
      { "Raptor", "RAPTOR" },
      { "Big Large Robot", "BIGLARGEROBOT" },
      { "   Foo Bar-Baz 42 ", "FOOBARBAZ42" },
      { "     ", "UNTITLED" },
    };
    
    for(String[] test : tests) {
      String name = test[0];
      Acronym expected = Acronym.fromString( test[1] );
      Acronym actual = UnitImpl.generatePlaceholderAcronym(name);
      assertEquals(expected, actual);      
    }
    
    
  }


  
  
}



















