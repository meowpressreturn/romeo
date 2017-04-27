package romeo.worlds.ui;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestWorldMapLogic {

  //TODO - this was copied from an old quick&dirty static test method and needs to be recoded as a proper unit test!
  @Test
  public void testCalcVisibility() {
    int[][] tests = new int[][] {
        //speed, distance, scanner
        //{1,2,3},
        { 100, 200, 120 }, { 123, 456, 789 }, { 50, 30, 25 }, { 50, 100, 25 }, { 50, 100, 80 }, { 50, 100, 200 },
        { 120, 1199, 420 }, { 120, 1200, 420 }, { 120, 1201, 420 }, { 120, 1200, 200 }, { 120, 1200, 100 },
        { 120, 1200, 80 }, { 50, 1234, 420 }, { 50, 1234, 200 }, { 1, 1000, 1 }, { 1, 1, 1 }, { 1, 1, 0 },
        { 100, 100, 0 }, { 120, 10000, 0 }, { 100, 100, 100 }, { 100, 0, 100 },

    };

    int fails = 0;
    for(int[] test : tests) {
      int speed = test[0];
      int distance = test[1];
      int scanner = test[2];
      int expected = WorldMapLogic.calcVisibilityOld(distance, speed, scanner);
      int vn = WorldMapLogic.calcVisibility(distance, speed, scanner);
      if(vn != expected) {
        fails++;
        System.out.println("FAILED" + " speed=" + speed + ",distance=" + distance + ",scanner=" + scanner
            + ", expected " + expected + ", found " + vn);
        WorldMapLogic.calcVisibility(distance, speed, scanner);
      } else {
        System.out
            .println("OK" + " speed=" + speed + ",distance=" + distance + ",scanner=" + scanner + ", found " + vn);
      }
    }
    System.out.println("Failures=" + fails);

    long howMany = 100000000;

    long startTime = System.currentTimeMillis();
    for(long i = 0; i < howMany; i++) {
      WorldMapLogic.calcVisibility(12345, 90, 420);
    }
    long newTime = System.currentTimeMillis() - startTime;

    startTime = System.currentTimeMillis();
    for(long i = 0; i < howMany; i++) {
      WorldMapLogic.calcVisibilityOld(12345, 90, 420);
    }
    long oldTime = System.currentTimeMillis() - startTime;

    long improvement = oldTime - newTime;

    System.out
        .println("New method is " + improvement + " ms faster than " + oldTime + " ms for " + howMany + " iterations");

    if(fails > 0) {
      fail("There were " + fails + " failures in the calcVisibility test");
    }
  }
  
  
}



















