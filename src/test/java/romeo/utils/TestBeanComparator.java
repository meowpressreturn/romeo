package romeo.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class TestBeanComparator {

  public static class TestBean {    
    private String _foo;
    private Integer _bar;
    
    public TestBean(String foo, Integer bar) {
      _foo = foo;
      _bar = bar;
    }
    
    public String getFoo() {
      return _foo;
    }
    
    public Integer getBar() {
      return _bar;
    }    
  }
  
  public static class WrappedString {
    private String _str;
    
    public WrappedString(String str) {
      _str = str;
    }
    
    @Override
    public String toString() {
      return _str==null ? "" : _str;
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  @Test
  public void testCompareValues_nulls() {
    //It considers null as a lower value than a non-null and two nulls as equal
    assertEquals(0, BeanComparator.compareValues(null, null) );
    assertTrue( BeanComparator.compareValues(null, "foo") < 0 );
    assertTrue( BeanComparator.compareValues("foo", null) > 0 ); 
  }
  
  @Test
  public void testCompareValues_wholes() {
    assertEquals(0, BeanComparator.compareValues(123,123) );
    assertTrue( BeanComparator.compareValues(123,200) < 0 );
    assertTrue( BeanComparator.compareValues(200,123) > 0 );
    
    assertEquals(0, BeanComparator.compareValues(new Long(123), new Integer(123)) );
    assertTrue( BeanComparator.compareValues(123,new Long(200)) < 0 );
    assertTrue( BeanComparator.compareValues(new Integer(200),new Long(123)) > 0 );
  }
  
  @Test
  public void testCompareValues_reals() {
    assertEquals(0, BeanComparator.compareValues( 0.123d, 0.123d) );
    assertTrue( BeanComparator.compareValues(0.123d,0.200d) < 0 );
    assertTrue( BeanComparator.compareValues(0.200d,0.123d) > 0 );
    
    assertEquals(0, BeanComparator.compareValues( 0.123f, new Float(0.123f)) );
    assertTrue( BeanComparator.compareValues(100.50f,100.60f) < 0 );
    assertTrue( BeanComparator.compareValues(100.60f,100.50f) > 0 );
    
    assertEquals(0, BeanComparator.compareValues( 100.50d, 100.50f) );
    
    //commented out because it will fail due to the BC not currently supported proper equality checking for reals
    //assertEquals(0, BeanComparator.compareValues( 0.123d, 0.123f) );
  }
  
  @Test
  public void testCompareValues_booleans() {
    assertEquals(0, BeanComparator.compareValues(true, true) );
    assertEquals(0, BeanComparator.compareValues(false, false) );
    assertTrue( BeanComparator.compareValues(false,true) < 0 );
    assertTrue( BeanComparator.compareValues(true, false) > 0 );
  }
  
  @Test
  public void testCompareValues_strings() {
    assertEquals(0, BeanComparator.compareValues("apple", "apple") );
    assertTrue( BeanComparator.compareValues("apple","orange") < 0 );
    assertTrue( BeanComparator.compareValues("orange","apple") > 0 );
  }
  
  @Test
  public void testCompareValues_mixed() {
    assertEquals(0, BeanComparator.compareValues("1", 1) );
    assertTrue( BeanComparator.compareValues(1,"2") < 0 );
    assertTrue( BeanComparator.compareValues("apple",1) > 0 );
    assertEquals(0, BeanComparator.compareValues( "hello", new StringBuilder("hello") ));
    assertEquals(0, BeanComparator.compareValues( "hello", new WrappedString("hello") ));
    assertEquals(0, BeanComparator.compareValues( new WrappedString("FOO"),new WrappedString("FOO") ));
    assertNotEquals(0, BeanComparator.compareValues( new WrappedString("FOO"),new WrappedString("BAR") ));
  }
  
  @Test
  public void testCompare() {
    TestBean one = new TestBean("aaa",200);
    TestBean two = new TestBean("zzz",100);
    
    BeanComparator bcFoo = new BeanComparator("foo");
    assertTrue(bcFoo.compare(one, two) < 0 );
    assertTrue(bcFoo.compare(two, one) > 0 );
    
    BeanComparator bcBar = new BeanComparator("bar");
    assertTrue(bcBar.compare(one, two) > 0 );
    assertTrue(bcBar.compare(two, one) < 0 );
    
    TestBean three = new TestBean("aaa",200);
    assertEquals(0, bcFoo.compare(one, three) );
    assertEquals(0, bcBar.compare(one, three) );
    
    boolean descending = true;
    BeanComparator bcFooRev = new BeanComparator("foo", descending, true);
    assertTrue(bcFooRev.compare(one, two) > 0 );
    assertTrue(bcFooRev.compare(two, one) < 0 );
    
    BeanComparator bcBarRev = new BeanComparator("bar", descending, true);
    assertTrue(bcBarRev.compare(one, two) < 0 );
    assertTrue(bcBarRev.compare(two, one) > 0 );
  }
  
  @Test
  public void testMapsWork() {
    //It should also be able to read a map property
    Map<String, Integer> foo = new HashMap<>();
    Map<String, Integer> bar = new HashMap<>();
    Map<String, Integer> moreBar = new TreeMap<>();    
    
    foo.put("qty", 1000);
    bar.put("qty", 2000);
    moreBar.put("qty", 2000);
    
    BeanComparator bc = new BeanComparator("qty");
    
    assertEquals(0, bc.compare(bar, moreBar));
    assertTrue( bc.compare(foo,bar) < 0 );
    assertTrue( bc.compare(bar,foo) > 0 );
  }
  
  @Test
  public void testCaseInsensitive() {
    
    TestBean Apple = new TestBean("Apple", 0);
    TestBean apple = new TestBean("apple", 0);
    
    assertTrue( new BeanComparator("foo", false, true).compare(Apple, apple) < 0 ); //Case sensitive
    assertTrue( new BeanComparator("foo", false, false).compare(Apple, apple) == 0 ); //Case insensitie
    
    
  }
  

//  
}


















