package romeo.utils;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class TestMutableDoubleListWrapper {
  
  private static final double D = 0.000000001;

  private MutableDoubleListWrapper _m;
  
  @Before
  public void testConstructor() {
    _m = new MutableDoubleListWrapper();
  }
  
  @Test
  public void testSetValueGetValue() {
    _m.setValue(5, 10000.64);
    assertEquals(0, _m.getValue(0), D);
    assertEquals(0, _m.getValue(1), D);
    assertEquals(0, _m.getValue(2), D);
    assertEquals(0, _m.getValue(3), D);
    assertEquals(0, _m.getValue(4), D);
    assertEquals(10000.64, _m.getValue(5), D);
    
    _m.setValue(5, 42);
    assertEquals(42, _m.getValue(5), D);    
    
    try {
      double d = _m.getValue(6);
      fail("expected IndexOutOfBoundsException but found " + d);
    } catch(IndexOutOfBoundsException expected) { }
  }
  
  @Test
  public void testAddValueGetValue() {    
    _m.addValue( 0, 5.123);
    _m.addValue( 6, 10);
    _m.addValue( 3, 5);
    _m.addValue( 3, 5);
    _m.addValue( 3, 5);
    
    assertEquals(5.123, _m.getValue(0), D );
    assertEquals(0, _m.getValue(1), D ); //ones in between should also be initialised
    assertEquals(10, _m.getValue(6), D );
    assertEquals(15, _m.getValue(3), D);
  }
  
  @Test
  public void testGetValue_uninitialised() {    
    try {
      double d = _m.getValue(0);
      fail("expected IndexOutOfBoundsException but found " + d);
    } catch(IndexOutOfBoundsException expected) { }
    try {
      double d = _m.getValue(4);
      fail("expected IndexOutOfBoundsException but found " + d);
    } catch(IndexOutOfBoundsException expected) { }
  }
  
  @Test
  public void testSetValue_sizing() {
    assertEquals(0, _m.getSize() );
    _m.setValue(7,12345d);
    assertEquals(8, _m.getSize());
  }
  
  @Test
  public void testAddValue_sizing() {
    assertEquals(0, _m.getSize() );
    _m.addValue(7,100);
    _m.addValue(7,150);
    assertEquals(8, _m.getSize());
  }
  
  @Test
  public void testSetSizeGetSize() {
   assertEquals(0,_m.getSize());
   try {
     double d = _m.getValue(0);
     fail("expected IndexOutOfBoundsException but found " + d);
   } catch(IndexOutOfBoundsException expected) { }
   _m.setSize(3);
   assertEquals(3, _m.getSize());
   assertEquals(0, _m.getValue(2), D);
   try {
     double d = _m.getValue(3);
     fail("expected IndexOutOfBoundsException but found " + d);
   } catch(IndexOutOfBoundsException expected) { }
  }
  
  @Test
  public void testGetData() {
    double[] expected = new double[] { 123, 456.7, 900, 42.59 };
    for(int i=0; i<expected.length;i++) {
      _m.setValue(i,expected[i]);
    }
    
    List<MutableDouble> data = _m.getData();
    assertEquals(4, data.size());
    assertEquals(_m.getSize(), data.size());
    for(int i=0; i<expected.length;i++) {
      assertEquals( expected[i], _m.getValue(i), D);
    }
    
    try {
      data.add( new MutableDouble(888) );
    } catch(UnsupportedOperationException expectedEx) {}
  }
  
  @Test
  public void testIterator() {
    double[] expected = new double[] { 123, 456.7, 900, 42.59,2372, 700, -2727, -999, 0 };
    for(int i=0; i<expected.length;i++) {
      _m.setValue(i,expected[i]);
    }
    
    Iterator<MutableDouble> i = _m.iterator();
    int index=0;
    while(i.hasNext()) {
      MutableDouble d = i.next();
      assertNotNull(d);
      assertEquals( expected[index], d.doubleValue(), D);
      index++;
    }
    assertEquals(expected.length, index);
    
    Iterator<?> i2 = _m.iterator();
    i2.next();
    try {
      i2.remove();
    } catch(UnsupportedOperationException expectedEx) {}
  }
}
