package romeo.fleet.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import romeo.units.api.Acronym;
import romeo.units.api.IUnit;
import romeo.units.impl.UnitImpl;

public class TestFleetElement {

  @Test
  public void testCompareTo() {
    
    IUnit aadvark = new UnitImpl(null, "Aardvark", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, Acronym.fromString("Aardvark"), null);
    IUnit budgie = new UnitImpl(null, "Budgie", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, Acronym.fromString("Budgie"), null);
    IUnit cocky = new UnitImpl(null, "Cocky", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, Acronym.fromString("Cocky"), null);
    IUnit dingo = new UnitImpl(null, "Dingo", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, Acronym.fromString("Dingo"), null);
   
    FleetElement a = new FleetElement(aadvark, 100, SourceId.fromInt(0) );
    FleetElement b = new FleetElement(budgie, 100, SourceId.fromInt(0) );
    FleetElement c = new FleetElement(cocky, 100, SourceId.fromInt(0) );
    FleetElement d = new FleetElement(dingo, 100, SourceId.fromInt(0) );
    
    FleetElement e = new FleetElement(aadvark, 100, SourceId.fromInt(1) );
    FleetElement f = new FleetElement(budgie, 100, SourceId.fromInt(1) );
    FleetElement g = new FleetElement(cocky, 100, SourceId.fromInt(1) );
    FleetElement h = new FleetElement(dingo, 100, SourceId.fromInt(1) );
    
    FleetElement i = new FleetElement(aadvark, 100, SourceId.fromInt(8) );
    FleetElement j = new FleetElement(budgie, 100, SourceId.fromInt(8) );
    FleetElement k = new FleetElement(cocky, 100, SourceId.fromInt(8) );
    FleetElement l = new FleetElement(dingo, 100, SourceId.fromInt(8) );
    
    FleetElement m = new FleetElement(aadvark, 100, SourceId.fromInt(9) );
    FleetElement n = new FleetElement(budgie, 100, SourceId.fromInt(9) );
    FleetElement o = new FleetElement(cocky, 100, SourceId.fromInt(9) );
    FleetElement p = new FleetElement(dingo, 100, SourceId.fromInt(9) );
    
    //Test it with sorting
    
    //All the same sourceId, we should just get alphabetical
    Random rnd = new Random(1234567890);
    FleetElement[] singleSourceExpected = { a,b,c,d }; 
    for(int foo=0; foo<256; foo++) {
      FleetElement[] singleSourceActual = shuffle(singleSourceExpected, rnd);
      Arrays.sort(singleSourceActual);
      assertArrayEquals("SingleSource", singleSourceExpected, singleSourceActual);
    }
    
    //Varied source id, should be by source id first, then alphabetical
    FleetElement[] expected = { a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p };    
    for(int foo=0; foo<256; foo++) {
      FleetElement[] actual = shuffle(expected, rnd);      
      Arrays.sort(actual);
      assertArrayEquals("foo=" + foo, expected, actual);
    }
    
  }
  
  private FleetElement[] shuffle(FleetElement[] input, Random rnd) {
    final int l = input.length;
    FleetElement[] output = new FleetElement[input.length];
    int j;
    for(int i=0; i<l; i++) {
      do { //Find an empty slot (We dont care about efficiency here)
        j = rnd.nextInt(l);
      } while( output[j] != null);
      output[j] = input[i];     
    }
    return output;
  }
  
}



















