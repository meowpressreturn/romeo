package romeo.units.api;

import java.util.Collection;
import java.util.Objects;

public class UnitUtils {
  /**
   * A rough index of how good it is for moving stuff about. Equals speed times
   * carry
   * @return logisticsFactor
   */
  public static int getLogisticsFactor(IUnit unit) {
    Objects.requireNonNull(unit, "unit must not be null");
    return unit.getSpeed() * unit.getCarry();
  }

  public static double[] getMultipliedOffenseRange(Collection<IUnit> units) {
    Objects.requireNonNull(units, "units must not be null");
    double[] ranges = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
    for(IUnit unit : units) {
      double mo = (double) (unit.getOffense() * unit.getAttacks());
      if(mo < ranges[0]) {
        ranges[0] = mo;
      }
      if(mo > ranges[1]) {
        ranges[1] = mo;
      }
    }
    return ranges;
  }

  public static double[] getLogisticsFactorRange(Collection<IUnit> units) {
    Objects.requireNonNull(units, "units must not be null");
    double[] ranges = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
    for(IUnit unit : units) {
      double lf = (double) getLogisticsFactor(unit);
      if(lf < ranges[0]) {
        ranges[0] = lf;
      }
      if(lf > ranges[1]) {
        ranges[1] = lf;
      }
    }
    return ranges;
  }
}
