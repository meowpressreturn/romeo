package romeo.worlds.impl;

import java.util.Objects;

import romeo.persistence.IdBean;
import romeo.units.api.UnitId;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.WorldId;

/**
 * Basic implementation of the IWorld interface. This class is still mutable.
 * Labout, capital and world ownership information are now in {@link IHistory}
 * objects.
 */
public class WorldImpl extends IdBean<WorldId> implements IWorld {
  private String _name;
  private int _worldX;
  private int _worldY;
  private UnitId _scannerId;
  private String _notes;
  private int _worldEi;
  private int _worldRer;

  /**
   * Construct a WorldImpl with values taken from the source IWorld
   * @param source
   */
  public WorldImpl(WorldId id, IWorld source) {
    this( id, 
        Objects.requireNonNull(source, "source may not be null").getName(),
        source.getWorldX(),
        source.getWorldY(),
        source.getScannerId(),
        source.getNotes(),
        source.getWorldEi(),
        source.getWorldRer() );
  }
  
  public WorldImpl(WorldId id, String name, int worldX, int worldY, UnitId scannerId, String notes, int worldEi, int worldRer) {
    setId(id);
    _name = Objects.requireNonNull(name, "name may not be null").trim();
    _worldX = worldX;
    _worldY = worldY;
    _scannerId = scannerId; //may be null
    _notes = Objects.requireNonNull(notes, "notes may not be null");
    _worldEi = worldEi;
    _worldRer = worldRer;    
  }

  /**
   * debugging messages
   * @return string
   */
  @Override
  public String toString() {
    return "World[id=" + getId() + ", name=" + getName() + "]";
  }

  /**
   * Returns the world name
   * @return name
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Returns the worlds x coordinate
   * @return x
   */
  @Override
  public int getWorldX() {
    return _worldX;
  }

  /**
   * Returns the worlds y coordinate
   * @return y
   */
  @Override
  public int getWorldY() {
    return _worldY;
  }

  /**
   * Return the romeo unit id of this worlds scanner.
   * @return scanner unit id
   */
  @Override
  public UnitId getScannerId() {
    return _scannerId;
  }

  /**
   * Returns the notes for this world. If there are none will return an empty
   * string.
   * @return notes the world notes or an empty string , never null
   */
  @Override
  public String getNotes() {
    return _notes == null ? "" : _notes;
  }

  /**
   * Gets the worlds EI value
   * @return EI
   */
  @Override
  public int getWorldEi() {
    return _worldEi;
  }

  /**
   * Gets the worlds RER value
   * @return RER
   */
  @Override
  public int getWorldRer() {
    return _worldRer;
  }
}
