package romeo.units.api;

import romeo.persistence.ICanGetId;
import romeo.utils.INamed;
import romeo.xfactors.api.XFactorId;

/**
 * Interface which exposes information about a unit
 */
public interface IUnit extends ICanGetId<UnitId>, Comparable<Object>, INamed {
  public boolean isCombatUnit();

  public double getFirepower();

  @Override
  public String getName();

  public int getAttacks();

  public int getOffense();

  public int getDefense();

  public int getPd();

  public int getSpeed();

  public int getCarry();

  public int getCost();

  public int getComplexity();

  public int getScanner();

  public int getLicense();

  public String getAcronym();

  public XFactorId getXFactor();
}