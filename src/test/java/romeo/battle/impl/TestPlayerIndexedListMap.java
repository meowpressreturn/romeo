package romeo.battle.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import romeo.utils.MutableDouble;

public class TestPlayerIndexedListMap {

  private static final double D = 0.00000001;
  
  private PlayerIndexedListMap _m;
  
  @Before
  public void setUp() {
    _m = new PlayerIndexedListMap(new String[] { "jupiter","saturn","neptune"} );
  }
  
  
  @Test
  public void testConstructor() {
    /**
     * Constructor. Requires a valid list of players. Will validate that list is
     * not null, not empty, and that each player named is not null or an empty
     * string and does not match any player already specified.
     * @param players
     *          an array of player names
     */
    
    //test null check
    try {
      new PlayerIndexedListMap(null);
      fail("expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    //test empty check
    try {
      new PlayerIndexedListMap(new String[] {});
      fail("expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
  //test empty element check
    try {
      new PlayerIndexedListMap(new String[] { "foo","","bar"});
      fail("expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
    
    //test null element check
    try {
      new PlayerIndexedListMap(new String[] { null,"foo","bar","baz"});
      fail("expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    //test duplicate check
    try {
      new PlayerIndexedListMap(new String[] { "foo","bar","baz","foo"});
      fail("expected IllegalArgumentException");
    }catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void getNoSuchPlayer() {
    try {
      _m.getValue("NoSuchPlayer",0);
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void addNoSuchPlayer() {
    try {
      _m.addValue("NoSuchPlayer",0,1234);
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void setNoSuchPlayer() {
    try {
      _m.setValue("NoSuchPlayer",0,1234);
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testAddValueGetValue() {    
    _m.addValue("jupiter", 0, 5.123);
    _m.addValue("jupiter", 6, 10);
    assertEquals(5.123, _m.getValue("jupiter",0), D );
    assertEquals(0, _m.getValue("jupiter",1), D ); //ones in between should also be initialised
    assertEquals(10, _m.getValue("jupiter",6), D );
    
    _m.addValue("saturn",3,5);
    _m.addValue("saturn",3,5);
    _m.addValue("saturn",3,5);
    assertEquals(15, _m.getValue("saturn",3), D);
    _m.addValue("saturn", 3, -20.0001);
    assertEquals(-5.0001, _m.getValue("saturn",3), D);
    
    //Uninitialised indexes should throw this exception
    try {
      double d = _m.getValue("neptune",0);
      fail("expected IndexOutOfBoundsException but found " + d);
    } catch(IndexOutOfBoundsException expected) { }
    try {
      double d = _m.getValue("saturn",4);
      fail("expected IndexOutOfBoundsException but found " + d);
    } catch(IndexOutOfBoundsException expected) { }
  }
  
  @Test
  public void testSetValueGetValue() {
    _m.setValue("neptune", 5, 10000.64);
    assertEquals(0, _m.getValue("neptune",0), D);
    assertEquals(0, _m.getValue("neptune",1), D);
    assertEquals(0, _m.getValue("neptune",2), D);
    assertEquals(0, _m.getValue("neptune",3), D);
    assertEquals(0, _m.getValue("neptune",4), D);
    assertEquals(10000.64, _m.getValue("neptune",5), D);
    
    _m.setValue("neptune", 5, 42);
    assertEquals(42, _m.getValue("neptune",5), D);    
    
    try {
      double d = _m.getValue("neptune",6);
      fail("expected IndexOutOfBoundsException but found " + d);
    } catch(IndexOutOfBoundsException expected) { }
  }
  
  @Test
  public void testSyncListSizes() {
    //this feature hasn't been implemented. When it is , change this test
    try {
      _m.syncListSizes();
      fail("expected UnsupportedOperationException");
    } catch(UnsupportedOperationException expected) {}
  }
  
  @Test
  public void testGetData() {
    //getData should return an unmodifiable list for the desired player
    _m.setValue("saturn",0,1.53);
    _m.setValue("saturn",3,10.02);
    _m.setValue("saturn",4,8.05);
    
    List<MutableDouble> data = _m.getData("saturn");
    assertEquals(5, data.size());
    assertEquals(10.02, data.get(3).doubleValue(), D);
    
    try {
      data.add( new MutableDouble(12345) );
    } catch(UnsupportedOperationException expected) {}     
    try {
      data.remove(3);
    } catch(UnsupportedOperationException expected) {}    
    
    for(Object element : data) {
      assertNotNull(element);
    }
  }
  
  @Test
  public void testForEach() {
    final List<Double> expectedValues = Arrays.asList( new Double[] { 100d,200d,300d,400d,567.89d } );
    for(int i=0; i<expectedValues.size(); i++) {
      _m.addValue("neptune",i, expectedValues.get(i));
    }
    
    final boolean[] seen = new boolean[expectedValues.size()];
    
    _m.forEach(new PlayerIndexedListMap.IVisitor() {      
      @Override
      public void visit(String player, int index, MutableDouble holder) {
        seen[index] = true;
        assertEquals(expectedValues.get(index), holder.doubleValue(), D);        
      }
    });
    
    for(int i=0; i<seen.length; i++) {
      assertTrue(seen[i]);
    }
  }
  
}
