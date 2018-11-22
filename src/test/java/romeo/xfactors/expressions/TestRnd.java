package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Test;

import romeo.battle.impl.RoundContext;

public class TestRnd {
  
  @Test
  public void testConstructor() {
    Rnd r = new Rnd(5, 10);
    assertEquals(5, r.getMin());
    assertEquals(10, r.getMax());
    
    //If mis-ordered it will automatically swap min and max
    Rnd r2 = new Rnd(0,100);
    assertEquals(0, r2.getMin() );
    assertEquals(100, r2.getMax() );
    
    //negatives are permitted
    new Rnd(-5,-10);
    new Rnd(-10,-100);
    
    new Rnd(0,1);
    new Rnd(1,0);
    new Rnd(10,11);
    
    try {
      Rnd r3 = new Rnd(10,10);
      fail("Expected IllegalArgumentException but found " + r3);
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testEvaluate() {
   
    RoundContext context = new RoundContext(new String[]{});    
    
    //lets empirically test it gives us about what we expect
    //theoretically this could fail for bounds and mean even if correct, but its vastly more
    //likely such a failure is a real error, thus investigation would be warranted
    final int samples = 1_000_000; //computers are pretty fast today huh?
    
    Object[][] tests = new Object[][] {
      //rnd, expMin, expMax, expMean (or null to ignore)
      { new Rnd(0,1), 0, 1, null  }, 
      { new Rnd(1,3), 1, 3, null  }, 
      { new Rnd(1,4), 1, 4, null  }, 
      { new Rnd(1,100), 1, 100, 50 },
      { new Rnd(-100,100), -100, 100, 0 },
          
      
    };
    
    for(int t=0; t<tests.length; t++) {
      Rnd r = (Rnd)tests[t][0];
      int expectedMin = (Integer)tests[t][1];
      int expectedMax = (Integer)tests[t][2];
      Integer expectedMean = (Integer)tests[t][3];
      int expectedMaxSum = (expectedMax+1-expectedMin) * samples;
      
      int observedSum = 0;
      int observedMin = Integer.MAX_VALUE;
      int observedMax = Integer.MIN_VALUE;         
      for(int i=0; i<samples; i++) {
        int result = (Integer)r.evaluate(context);
        if(result<observedMin) { observedMin = result; }
        if(result>observedMax) { observedMax = result; }
        observedSum += result;
      } //end for i
      int observedMean = observedSum / samples;      
      
      assertEquals( "[" + t + "] min should be " + expectedMin, expectedMin, observedMin );
      assertEquals( "[" + t + "] max should be " + expectedMax, expectedMax, observedMax );
      if(expectedMean != null) {
        assertEquals( "[" + t + "] mean should be " + expectedMean, expectedMean.intValue(), observedMean );
      }
      assertTrue( "[" + t + "] sum should not have exceeded " + expectedMaxSum + " but found " + observedSum, observedSum <= expectedMaxSum);
    } //end for t
    
    
    try {
      new Rnd(0,500).evaluate(null);
      fail("expected a NullPointerException");
    } catch(NullPointerException expected) {}
    
    
  }
  
  
}



















