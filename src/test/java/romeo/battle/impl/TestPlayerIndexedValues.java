package romeo.battle.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestPlayerIndexedValues {
  
  private static final double D = 0.00000000001;

  private PlayerIndexedValues _piv;
  
  @Before
  public void createPiv() {
    _piv = new PlayerIndexedValues(new String[] { "Venus","Earth","Mars"} );
  }
  
  @Test 
  public void testConstructor() {
    try {
      _piv = new PlayerIndexedValues(null);
      fail("expected NullPointerException");
    } catch(NullPointerException npe) {}
    
    //At least one player name should be specified
    try {
      _piv = new PlayerIndexedValues(new String[] { });
      fail("expected IllegalArgumentException");
    } catch( IllegalArgumentException expected) {}
    
    try {
      _piv = new PlayerIndexedValues(new String[] { "foo",null,"baz"});
      fail("expected IllegalArgumentException");
    } catch( IllegalArgumentException expected) {}
    
    //empty players names arent allowed
    try {
      _piv = new PlayerIndexedValues(new String[] { "foo","","baz"});
      fail("expected IllegalArgumentException");
    } catch( IllegalArgumentException expected) {}
    
    //these should work
    _piv = new PlayerIndexedValues(new String[] { "foo" } );
    _piv = new PlayerIndexedValues(new String[] { "foo with a space in mame" } );
    _piv = new PlayerIndexedValues(new String[] { "foo","bar","baz" } );
    
  }
  
  @Test
  public void testAddValue() {
    _piv.addValue("Venus", 123.40);
    assertEquals(123.40d, _piv.getValue("Venus"), D);
    assertEquals(0, _piv.getValue("Earth"), D);
    assertEquals(0, _piv.getValue("Mars"), D); 
    _piv.addValue("Venus", 3);
    _piv.addValue("Venus", -6);
    assertEquals(120.4, _piv.getValue("Venus"), D);
  }
  
  @Test
  public void testGetValue() {
    assertEquals(0, _piv.getValue("Earth"), D);
    
    //Retrieval for an unregistered player should throw an exception
    try {
      double d = _piv.getValue("no such player");
      fail("Expected IllegalArgumentException but found " + d);
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testSetValue() {
    _piv.setValue("Mars",1024);
    assertEquals(1024, _piv.getValue("Mars"), D);
    _piv.setValue("Mars",2048);
    assertEquals(2048, _piv.getValue("Mars"), D); 
  }
  
  @Test
  public void testGetPlayers() {
    String[] players = _piv.getPlayers();
    assertArrayEquals( new String[] {"Venus","Earth","Mars"}, players);
  }
  
  @Test
  public void testGetTotal() {
    _piv.addValue("Venus", 100);
    _piv.addValue("Venus", 200);
    _piv.setValue("Earth", 1000);
    _piv.setValue("Mars", 1234);
    _piv.setValue("Mars", 2000);
    _piv.addValue("Earth",-50);
    assertEquals(3250, _piv.getTotal(), D);
  }
  
  @Test
  public void testClear() {
    _piv.addValue("Venus", 100);
    _piv.addValue("Venus", 200);
    _piv.setValue("Earth", 1000);
    _piv.setValue("Mars", 1234);
    _piv.setValue("Mars", 2000);
    _piv.addValue("Earth",-50);
    _piv.clear();
    assertEquals(0, _piv.getValue("Venus"), D);
    assertEquals(0, _piv.getValue("Mars"), D);
    assertEquals(0, _piv.getValue("Earth"), D);
  }
  
  @Test
  public void testGetPercentage() {
    
    try {
      double mars = _piv.getPercentage("Mars");
      fail("Expected an IllegalStateException but found " + mars);
    } catch(IllegalStateException expected) {} 
    
    _piv.setValue("Venus",300);
    _piv.setValue("Earth",100);
    _piv.setValue("Mars",600);
    
    try {
      double d = _piv.getPercentage("NoSuchPlayer");
      fail("Expected an IllegalArgumentException but found " + d);
    }catch(IllegalArgumentException expected) {}
    
    assertEquals(1000, _piv.getTotal(), D);
    double venus = _piv.getPercentage("Venus");
    assertEquals(0.3, venus, D);
    assertEquals(0.6, _piv.getPercentage("Mars"), D);
    
  }
  
  @Test
  public void testGetAverage() {
    
    assertEquals(0, _piv.getAverage(), D);
    
    _piv.setValue("Venus",300);
    _piv.setValue("Earth",300);
    _piv.setValue("Mars",600);
    
    assertEquals(400, _piv.getAverage(), D);
  }
  
}
