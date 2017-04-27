package romeo.worlds.api;

import romeo.persistence.ICanGetId;
import romeo.units.api.UnitId;
import romeo.utils.INamed;

/**
 * Interface to an object that provides information about a world. To modify
 * information you will need to create a new instance of an IWorld
 * implementation and copy the data in and make the required changes. (WorldImpl
 * has a constructor to facilitate this.)
 */
public interface IWorld extends INamed, ICanGetId<WorldId> {
  @Override
  public String getName();

  public int getWorldX();

  public int getWorldY();

  /**
   * Returns the id of the unit that represents this worlds scanner type.
   * If this is null then the world has no scanner and the default scannerRange
   * as defined in settings should be used.
   * @return
   */
  public UnitId getScannerId();

  public String getNotes();

  public int getWorldEi();

  public int getWorldRer();
}
