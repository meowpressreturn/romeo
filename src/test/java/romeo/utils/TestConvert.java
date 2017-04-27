package romeo.utils;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class TestConvert {
  
  private static final double D = 0.00001; //floats can be quite innacurate huh?
  
  @Test
  public void testToCsv() {
    Object[][] tests = new Object[][] {
      { new String[] { "" }, "" },
      { new String[] { "foo","bar","baz"} , "foo,bar,baz" },
      { new String[] { "fo\no","bar","baz\n"} , "\"fo\no\",bar,\"baz\n\"" },
      { new String[] { "foo","b,a,r","baz"} , "foo,\"b,a,r\",baz" },
      { new String[] { "foo","bar","b\"az"} , "foo,bar,\"b\"\"az\"" },
      { new String[] { "  \" "}, "\"  \"\" \""},
      { new String[] { "","bar"} , ",bar" },
      { new String[] { "","",""} , ",," },
      
      { null, new NullPointerException("fieldValuesList may not be null") },
      { new String[] { }, new IllegalArgumentException("fieldValuesList may not be empty") },
      { new String[] { "",null,""} , new NullPointerException("fieldValuesList may not contain null elements") },
      
    };
    
    int index=0;
    for(Object[] test : tests) {
      List<String> testValue = (test[0]==null) ? null : Arrays.asList( (String[])test[0] );
      String expected = (test[1] instanceof String) ? (String)test[1] : null;
      
      try {
        String actual = Convert.toCsv(testValue);
        if(expected != null) {          
          assertEquals("tests[ " + index + "]", expected, actual);
        }  else {
          
          Exception expectedError = (Exception)test[1];
          fail("tests[ " + index + "] expected to catch " + expectedError.getClass().getName() 
              + " : " + expectedError.getMessage()
              + " , actual result=" + actual);
        }
      } catch(Exception e) {
        if(expected==null) {
          Exception expectedError = (Exception)test[1];
          assertEquals("tests[" + index + "]" , expectedError.getClass().getName(), e.getClass().getName());
          assertEquals("tests[" + index + "]" , expectedError.getMessage(), e.getMessage());
          //nb: trying to do assertEquals() on the actual exception objects doesn't work as intended here
        } else {
          throw e;
        }
      }
      index++;
    }
  }
  
  @Test
  public void testFromCsv() {
    
    Object[][] tests = new Object[][] {
      
      
      //FAIL cases
      { null, new NullPointerException("csv may not be null") },
      { "\"foo", new IllegalArgumentException("unterminated quote at position 0")  }, 
      { "foo,\"bar,baz", new IllegalArgumentException("unterminated quote at position 4") }, 
      { "fo\"\"o", new IllegalArgumentException("unquoted quote at position 2") },
      { " \"", new IllegalArgumentException("unquoted quote at position 1") },
      { "\"abc\"def\"", new IllegalArgumentException("unescaped quote at position 4") },
      { "foo\nbar", new IllegalArgumentException("unquoted newline at position 3") },
      { "\"foo\",hello,bar\nbaz", new IllegalArgumentException("unquoted newline at position 15") },
      
      //WIN cases
      { "", new String[] { "" }                                 },
      { ",", new String[] { "","" }                             },
      { ",,,", new String[] { "","","","" }                     },
      { ",,,,", new String[] { "","","","","" }                 },
      { ", ", new String[] { ""," " }                           },
      { ",\"\"", new String[] { "","" }                         },
      { "\"\",\"\"", new String[] { "","" }                     },
      { " ", new String[] { " " }                               },
      { "foo", new String[] { "foo"}                            },
      { "foo,bar,baz", new String[] { "foo","bar","baz"}        },
      { "\"foo\"" , new String[] { "foo" }                      },
      { "\"foo\",\"bar\"" , new String[] { "foo","bar" }        },
      { "\"fo\"\"o\"" , new String[] { "fo\"o" }                },
      { "\"foo\nbar\"", new String[] { "foo\nbar" }             },
      { "\"foo\",hello,\"bar\n\"\"baz\"", new String[] {"foo","hello","bar\n\"baz" } },
      
    };
    
    int index=0;
    for(Object[] test : tests) {
      String testValue = (String)test[0];
      String[] expected = (test[1] instanceof String[]) ? (String[])test[1] : null;
      
      try {
        List<String> actual = Convert.fromCsv(testValue);
        if(expected != null) {
          assertEquals("tests["+index+"] length", expected.length, actual.size());
          for(int i=0; i<expected.length; i++) {
            assertEquals("tests["+index+"] column " + i, expected[i], actual.get(i)); 
          }
        } else {
          Exception expectedError = (Exception)test[1];
          fail("tests["+index+"] expected to catch " + expectedError.getClass().getName() 
              + " : " + expectedError.getMessage()
              + " , actual result=" + actual);
        }
      } catch(Exception e) {
        if(expected==null) {
          Exception expectedError = (Exception)test[1];
          assertEquals("tests["+index+"]", expectedError.getClass().getName(), e.getClass().getName());
          assertEquals("tests["+index+"]", expectedError.getMessage(), e.getMessage());
        } else {
          throw e;
        }
      }
      index++;
    }
  }
  
  @Test
  public void testGetIndexOfItem() {
    assertEquals(2, Convert.getIndexOfItem(new int[] {5,5,5} , 11) );
    assertEquals(0, Convert.getIndexOfItem(new int[] {5,5,5} , 0) );
    assertEquals(1, Convert.getIndexOfItem(new int[] {5,5,5} , 5) );
    assertEquals(2, Convert.getIndexOfItem(new int[] {5,5,5} , 10) );
    
    try {
      int i = Convert.getIndexOfItem( new int[] { 5 } , 5);
      fail("Expected an IndexOutOfBoundsException, but found " + i);
    }catch(IndexOutOfBoundsException expected) { }
    
    try {
      int i = Convert.getIndexOfItem( new int[] { } , 0);
      fail("Expected an IndexOutOfBoundsException, but found " + i);
    }catch(IndexOutOfBoundsException expected) { }
    
    try {
      int i = Convert.getIndexOfItem( new int[] { 0 } , 0);
      fail("Expected an IndexOutOfBoundsException, but found " + i);
    }catch(IndexOutOfBoundsException expected) { }
    
    try {
      int i = Convert.getIndexOfItem( null , 0);
      fail("Expected a NullPointerException, but found " + i);
    }catch(NullPointerException expected) { }
      
  }
  
  @Test
  public void testReplace() {
    assertEquals("alphaXXXcharlie", Convert.replace("alphabravocharlie", "bravo", "XXX") );
    assertEquals("XXXbravocharlie", Convert.replace("alphabravocharlie", "alpha", "XXX") );
    assertEquals("alphabravoXXX", Convert.replace("alphabravocharlie", "charlie", "XXX") );
    assertEquals("alphabravocharlie", Convert.replace("alphabravocharlie", "delta", "XXX") );
    
    assertEquals("alphabravocharlie", Convert.replace("alphabravocharlie", "alpha", "alpha") );
    assertEquals("alphaalphabravocharlie", Convert.replace("alphabravocharlie", "alpha", "alphaalpha") );
    
    assertEquals("alphacharlie", Convert.replace("alphabravocharlie", "bravo", "") );
    
    //It treats a null newStr as an emoty string
    assertEquals("bravocharlie", Convert.replace("alphabravocharlie", "alpha", null) );
    
    //multi replace
    assertEquals("AlphAbrAvochArlie", Convert.replace("alphabravocharlie", "a", "A") );
    assertEquals("lphbrvochrlie", Convert.replace("alphabravocharlie", "a", "") );
    
    assertEquals("hoppxnoppxsoppx", Convert.replace("happynappysappy", "appy", "oppx") );
    
    try{
      String result = Convert.replace("", "", "alphabravocharlie");
      fail("expected IllegalArgumentException but found " + result);
    } catch(IllegalArgumentException expected) {}
    
    try {
      String result = Convert.replace(null, "bravo", "XXX");
      fail("expected NullPointerException but found " + result);
    } catch(NullPointerException expected) {}
    
    try {
      String result = Convert.replace("alphabtravocharlie", null, "XXX");
      fail("expected NullPointerException but found " + result);
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testRemoveNulls() {
    List<String> list = new ArrayList<>( Arrays.asList(null,"foo","bar",null,"baz","bob",null) ); //nb: list must be mutable
    Convert.removeNulls(list);
    assertArrayEquals(new String[] { "foo","bar","baz","bob"}, list.toArray()); 
    try {
      Convert.removeNulls(null);
      fail("expected a NullPointerException");
    } catch(NullPointerException npe) { }
  }
  
  @Test
  public void testToPrimitiveIntArray() {
    {
      Set<Integer> set = new TreeSet<>(Arrays.asList(0,6,2,50,4000,12,45,34,-18,-32,1234));
      int[] ints = Convert.toPrimitiveIntArray(set);
      assertArrayEquals( new int[] { -32,-18,0,2,6,12,34,45,50,1234,4000}, ints);
    }
    
    assertArrayEquals( 
        new int[] { 1000,23,45,11,56 }, 
        Convert.toPrimitiveIntArray( Arrays.asList(1000,23,45,11,56)));
    
    assertArrayEquals( 
        new int[] { }, 
        Convert.toPrimitiveIntArray( new LinkedList<Integer>() ) );
    
    assertNull( Convert.toPrimitiveIntArray(null) );
    
    assertArrayEquals( 
        new int[] { 1,2,3,4 }, 
        Convert.toPrimitiveIntArray( Arrays.asList(1.5d,2.7d,3.1d,4.0d)));
    
    
    assertArrayEquals( 
        new int[] { 1,2,3,4 }, 
        Convert.toPrimitiveIntArray( Arrays.asList(1.5f,2.7f,3.1f,4.0f)));
    
    assertArrayEquals( 
        new int[] { 1,2,0,4 }, 
        Convert.toPrimitiveIntArray( Arrays.asList(1,2,null,4)));
  }
  
  @Test
  public void testToDouble() {
    Map.Entry<?,?> entry = new Map.Entry<String, Float>() {
      @Override
      public String getKey() { return "test"; }
      @Override
      public Float getValue() { return 123.45f; };

      @Override
      public Float setValue(Float value) { throw new UnsupportedOperationException(); }
    };
    
    //null
    assertEquals( 0d, Convert.toDouble(null), D );
    //numbers
    assertEquals( 42d, Convert.toDouble( new Double(42.0) ), D );
    assertEquals( 42d, Convert.toDouble( new Float(42.0) ), D );
    assertEquals( 42d, Convert.toDouble( new Integer(42) ), D );
    assertEquals( 42d, Convert.toDouble( new Long(42) ), D );
    assertEquals( 42d, Convert.toDouble( new Short((short)42) ), D );
    assertEquals( 42d, Convert.toDouble( new BigDecimal(42d) ), D );
    assertEquals( 42d, Convert.toDouble( new BigInteger("42") ), D );
    //bools
    assertEquals( 1d, Convert.toDouble( true ), D );
    assertEquals( 0d, Convert.toDouble( false ), D );
    //map entry
    assertEquals( 123.45d, Convert.toDouble( entry) , D );
    //everything else is based on the toString
    assertEquals( 123.45d, Convert.toDouble("123.45") , D );
    assertEquals( 123.45d, Convert.toDouble(new StringBuffer("123.45")) , D );
    assertEquals( -1000d, Convert.toDouble("-1000") , D );
    //those that don't parse will return 0 rather than an exception
    assertEquals( 0d, Convert.toDouble("hello") , D );
    assertEquals( 0, Convert.toDouble("") , D );
  }
  
  @Test
  public void testTrimStringsInPlace() {
   
    List<String> strings = new ArrayList<>( Arrays.asList("foo ","bar","   \nbaz ","  bob ","hello",null,"sailor\t\t\n"));
    Convert.trimStringsInPlace(strings);
    assertEquals("foo",strings.get(0));
    assertEquals("sailor",strings.get(6));
    assertNull(strings.get(5));
    assertEquals(7,strings.size());
    
    try {
      Convert.trimStringsInPlace(null);
      fail();
    } catch(NullPointerException expected) {}
    
    Convert.trimStringsInPlace(new ArrayList<String>());
    
  }
}






























